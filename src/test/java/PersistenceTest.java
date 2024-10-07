import com.sparta.entity.Memo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PersistenceTest {
    EntityManagerFactory emf;
    EntityManager em;
    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("memo");
        em = emf.createEntityManager();
    }

    /*- 영속성 컨텍스트는 내부적으로 캐시 저장소를 가지고 있습니다.
    - 우리가 저장하는 Entity 객체들이 1차 캐시 즉, 캐시 저장소에 저장된다고 생각하시면됩니다.
    - 캐시 저장소는 Map 자료구조 형태로 되어있습니다.
        - **key**에는 @Id로 매핑한 기본 키 즉, 식별자 값을 저장합니다.
        - **value**에는 해당 Entity 클래스의 객체를 저장합니다.
        - 영속성 컨텍스트는 캐시 저장소 **Key**에 저장한 식별자값을 사용하여 Entity 객체를 구분하고 관리합니다.*/
    @Test
    @DisplayName("1차 캐시 : Entity 저장")
    void test1() {
        EntityTransaction et = em.getTransaction();

        et.begin();  // 트랜잭션을 시작하는 명령어

        try {

            Memo memo = new Memo();
            memo.setId(1L);
            memo.setUsername("Robbie");
            memo.setContents("1차 캐시 Entity 저장");

            em.persist(memo);  // EntityManager.persist() : DB에 인자를 저장

            et.commit();  // 트랜잭션의 작업들을 영구적으로 DB에 반영하는 명령어

        } catch (Exception ex) {
            ex.printStackTrace();  // Prints this throwable and its backtrace to the specified print stream.
            et.rollback();  // 오류가 발생했을 때 트랜잭션의 작업을 모두 취소하고, 이전 상태로 되돌리는 명령어
        } finally {
            em.close();
        }

        emf.close();
    }

    @Test
    @DisplayName("Entity 조회 : 캐시 저장소에 해당하는 Id가 존재하지 않은 경우")
    void test2() {
        try {
            // DB에서 데이터를 조회만 하는 경우에는 데이터의 변경이 발생하는 것이 아니기 때문에 트랜잭션이 없어도 조회가 가능합니다.
            // Memo memo = em.find(Memo.class, 1); 호출 시 캐시 저장소에 해당 값이 존재하지 않기 때문에 DB에 SELECT 조회하여 캐시 저장소에 저장한 후 반환합니다.
            Memo memo = em.find(Memo.class, 1);
            System.out.println("memo.getId() = " + memo.getId());
            System.out.println("memo.getUsername() = " + memo.getUsername());
            System.out.println("memo.getContents() = " + memo.getContents());


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }

    @Test
    @DisplayName("Entity 조회 : 캐시 저장소에 해당하는 Id가 존재하는 경우")
    void test3() {
        try {
            // em.find(Memo.class, 1); 호출 시 캐시 저장소에 식별자 값이 1이면서 Memo Entity 타입인 값이 있는지 조회합니다.
            Memo memo1 = em.find(Memo.class, 1);
            System.out.println("memo1 조회 후 캐시 저장소에 저장\n");

            Memo memo2 = em.find(Memo.class, 1);
            System.out.println("memo2.getId() = " + memo2.getId());
            System.out.println("memo2.getUsername() = " + memo2.getUsername());
            System.out.println("memo2.getContents() = " + memo2.getContents());


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            em.close();
        }

        emf.close();
    }

    // **'1차 캐시'** 사용의 장점
    //    1. DB 조회 횟수를 줄임
    //    2. **'1차 캐시'**를 사용해 DB row 1개 당 객체 1개가 사용되는 것을 보장 (객체 동일성 보장)
    @Test
    @DisplayName("객체 동일성 보장")
    void test4() {
        EntityTransaction et = em.getTransaction();

        et.begin();

        try {
            Memo memo3 = new Memo();
            memo3.setId(2L);
            memo3.setUsername("Robbert");
            memo3.setContents("객체 동일성 보장");
            em.persist(memo3);

            Memo memo1 = em.find(Memo.class, 1);
            Memo memo2 = em.find(Memo.class, 1);
            Memo memo  = em.find(Memo.class, 2);

            System.out.println(memo1 == memo2);
            System.out.println(memo1 == memo);

            et.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            et.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }


    @Test
    @DisplayName("Entity 삭제")
    void test5() {
        EntityTransaction et = em.getTransaction();

        et.begin();

        try {
            // 삭제할 Entity를 조회한 후 캐시 저장소에 없다면 DB에 조회해서 저장
            Memo memo = em.find(Memo.class, 2);
            // em.remove(memo); 호출 시 삭제할 Entity를 DELETED 상태로 만든 후 트랜잭션 commit 후 Delete SQL이 DB에 요청됩니다.
            em.remove(memo);

            et.commit();

        } catch (Exception ex) {
            ex.printStackTrace();
            et.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }

    /*
    JPA는 트랜잭션처럼 작동하기 위해 쓰기 지연 저장소를 만들어 SQL을 모아두고 있다가 트랜잭션 commit 후 한번에 DB에 반영합니다. */
    @Test
    @DisplayName("쓰기 지연 저장소 확인")
    void test6() {
        EntityTransaction et = em.getTransaction();

        et.begin();

        try {
            Memo memo = new Memo();
            memo.setId(2L);
            memo.setUsername("Robbert");
            memo.setContents("쓰기 지연 저장소");
            em.persist(memo);

            Memo memo2 = new Memo();
            memo2.setId(3L);
            memo2.setUsername("Bob");
            memo2.setContents("과연 저장을 잘 하고 있을까?");
            em.persist(memo2);

            System.out.println("트랜잭션 commit 전");
            et.commit();
            System.out.println("트랜잭션 commit 후");

        } catch (Exception ex) {
            ex.printStackTrace();
            et.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }

    /*- 사실 트랜잭션 commit 후 추가적인 동작이 있는데 바로 `em.flush();` 메서드의 호출입니다.
      - flush 메서드는 영속성 컨텍스트의 변경 내용들을 DB에 반영하는 역할을 수행합니다. */
    @Test
    @DisplayName("flush() 메서드 확인")
    void test7() {
        // Insert, Update, Delete 즉, 데이터 변경 SQL을 DB에 요청 및 반영하기 위해서는 트랜잭션이 필요합니다.
        EntityTransaction et = em.getTransaction();

        et.begin();

        try {
            Memo memo = new Memo();
            memo.setId(4L);
            memo.setUsername("Flush");
            memo.setContents("Flush() 메서드 호출");
            em.persist(memo);

            System.out.println("flush() 전");
            em.flush(); // flush() 직접 호출
            System.out.println("flush() 후\n");


            System.out.println("트랜잭션 commit 전");
            et.commit();
            System.out.println("트랜잭션 commit 후");

        } catch (Exception ex) {
            ex.printStackTrace();
            et.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
    /*- JPA에서는 Update를 어떻게 처리하는지 살펴보겠습니다.
    - JPA는 영속성 컨텍스트에 Entity를 저장할 때 최초 상태(LoadedState)를 저장합니다.
    - 트랜잭션이 commit되고 `em.flush();` 가 호출되면 Entity의 현재 상태(entityInstance)와 저장한 최초 상태를 비교합니다.
    - 변경 내용이 있다면 Update SQL을 생성하여 쓰기 지연 저장소에 저장하고 모든 쓰기 지연 저장소의 SQL을 DB에 요청합니다.
    - 마지막으로 DB의 트랜잭션이 commit 되면서 반영됩니다.
    - 따라서 변경하고 싶은 데이터가 있다면 먼저 데이터를 조회하고 해당 Entity 객체의 데이터를 변경하면 자동으로 Update SQL이 생성되고 DB에 반영됩니다.
    - 이러한 과정을 변경 감지, Dirty Checking이라 부릅니다.*/
    @Test
    @DisplayName("변경 감지 확인")
    void test8() {
        EntityTransaction et = em.getTransaction();

        et.begin();

        try {
            System.out.println("변경할 데이터를 조회합니다.");
            Memo memo = em.find(Memo.class, 4);
            System.out.println("memo.getId() = " + memo.getId());
            System.out.println("memo.getUsername() = " + memo.getUsername());
            System.out.println("memo.getContents() = " + memo.getContents());

            System.out.println("\n수정을 진행합니다.");
            memo.setUsername("Update");
            memo.setContents("변경 감지 확인");

            System.out.println("트랜잭션 commit 전");
            et.commit();
            System.out.println("트랜잭션 commit 후");

        } catch (Exception ex) {
            ex.printStackTrace();
            et.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
