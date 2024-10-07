import com.sparta.entity.Memo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EntityStateTest {
    EntityManagerFactory emf;
    EntityManager em;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("memo");
        em = emf.createEntityManager();
    }

    @Test
    @DisplayName("비영속과 영속 상태")
    void test1() {
        EntityTransaction et = em.getTransaction();

        et.begin();

        try {

            Memo memo = new Memo(); // 비영속 상태
            memo.setId(1L);
            memo.setUsername("Robbie");
            memo.setContents("비영속과 영속 상태");

            memo.setId(2L);
            memo.setUsername("Robbert");
            memo.setContents("비영속과 영속 상태2");

            em.persist(memo);

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
    @DisplayName("준영속 상태 : detach()")
    void test2() {
        EntityTransaction et = em.getTransaction();

        et.begin();

        try {

            Memo memo = em.find(Memo.class, 1);
            System.out.println("memo.getId() = " + memo.getId());
            System.out.println("memo.getUsername() = " + memo.getUsername());
            System.out.println("memo.getContents() = " + memo.getContents());

            // em.contains(entity) : Entity 객체가 현재 영속성 컨텍스트에 저장되어 관리되는 상태인지 확인하는 메서드
            System.out.println("em.contains(memo) = " + em.contains(memo));

            System.out.println("detach() 호출");
            em.detach(memo); // 특정 Entity 하나를 준영속 상태로 만든다.
            System.out.println("em.contains(memo) = " + em.contains(memo));

            // memo가 준영속상태이기 때문에 아래 71번 72번 줄은 무의미
            System.out.println("memo Entity 객체 수정 시도");
            memo.setUsername("Update");
            memo.setContents("memo Entity Update");

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

    @Test
    @DisplayName("준영속 상태 : clear()")
    void test3() {
        EntityTransaction et = em.getTransaction();

        et.begin();

        try {
            Memo memo1 = em.find(Memo.class, 1);
            Memo memo2 = em.find(Memo.class, 2);

            // em.contains(entity) : Entity 객체가 현재 영속성 컨텍스트에 저장되어 관리되는 상태인지 확인하는 메서드
            System.out.println("em.contains(memo1) = " + em.contains(memo1));
            System.out.println("em.contains(memo2) = " + em.contains(memo2));

            System.out.println("clear() 호출");
            em.clear();  // 영속성 컨텍스트를 초기화, 그 안에 있던 모든 Entity -> 준영속 상태
            System.out.println("em.contains(memo1) = " + em.contains(memo1));
            System.out.println("em.contains(memo2) = " + em.contains(memo2));

            // 영속성 컨텍스트가 사라진 게 아니기 때문에 다시 조회하고 업데이트 가능
            System.out.println("memo#1 Entity 다시 조회");
            Memo memo = em.find(Memo.class, 1);
            System.out.println("em.contains(memo) = " + em.contains(memo));
            System.out.println("\n memo Entity 수정 시도");
            memo.setUsername("Update");
            memo.setContents("memo Entity Update");

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

    @Test
    @DisplayName("준영속 상태 : close()")
    void test4() {
        EntityTransaction et = em.getTransaction();

        et.begin();

        try {

            Memo memo1 = em.find(Memo.class, 1);
            Memo memo2 = em.find(Memo.class, 2);

            // em.contains(entity) : Entity 객체가 현재 영속성 컨텍스트에 저장되어 관리되는 상태인지 확인하는 메서드
            System.out.println("em.contains(memo1) = " + em.contains(memo1));
            System.out.println("em.contains(memo2) = " + em.contains(memo2));

            System.out.println("close() 호출");
            em.close();  // 영속성 컨텐스트를 아예 종료시킴. 따라서 아래 코드 실행 불가.
            Memo memo = em.find(Memo.class, 2); // Session/EntityManager is closed 메시지와 함께 오류 발생
            System.out.println("memo.getId() = " + memo.getId());

        } catch (Exception ex) {
            ex.printStackTrace();
            et.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }

    @Test
    @DisplayName("merge() : 저장")
    void test5() {
        EntityTransaction et = em.getTransaction();

        et.begin();

        try {

            Memo memo = new Memo();
            memo.setId(3L);
            memo.setUsername("merge()");
            memo.setContents("merge() 저장");

            System.out.println("merge() 호출");
            Memo mergedMemo = em.merge(memo);

            System.out.println("em.contains(memo) = " + em.contains(memo));  // false
            System.out.println("em.contains(mergedMemo) = " + em.contains(mergedMemo));  // true

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

    @Test
    @DisplayName("merge() : 수정")
    void test6() {
        EntityTransaction et = em.getTransaction();

        et.begin();

        try {

            Memo memo = em.find(Memo.class, 3);
            System.out.println("memo.getId() = " + memo.getId());
            System.out.println("memo.getUsername() = " + memo.getUsername());
            System.out.println("memo.getContents() = " + memo.getContents());

            System.out.println("em.contains(memo) = " + em.contains(memo));

            System.out.println("detach() 호출");
            em.detach(memo); // 준영속 상태로 전환
            System.out.println("em.contains(memo) = " + em.contains(memo));

            System.out.println("준영속 memo 값 수정");
            memo.setContents("merge() 수정");

            System.out.println("\n merge() 호출");
            Memo mergedMemo = em.merge(memo);
            System.out.println("mergedMemo.getContents() = " + mergedMemo.getContents()); // merge() 수정

            System.out.println("em.contains(memo) = " + em.contains(memo));  // false
            System.out.println("em.contains(mergedMemo) = " + em.contains(mergedMemo));  //  true

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

/*
- 비영속 상태:
영속성 컨텍스트에 저장되지 않았기 때문에 JPA의 관리를 받지 않습니다.

- 영속(Managed):
persist(entity)를 통해 비영속 Entity를 EntityManager가 영속성 컨텍스트에 저장하여 관리되고 있는 상태로 만듭니다.

- 준영속(Detached):
영속성 컨텍스트에 저장되어 관리되다가 분리된 상태,
- 영속 상태에서 준영속 상태로 바꾸는 방법: detach(entity), clear(), close()

- merge(entity):
전달받은 Entity를 사용하여 새로운 영속 상태의 Entity를 반환합니다.
    **merge(entity)** 동작: 비영속, 준영속 모두 파라미터로 받을 수 있으며 상황에 따라 ‘저장’을 할 수도 ‘수정’을 할 수도 있습니다.
    - 파라미터로 전달된 Entity의 식별자 값으로 영속성 컨텍스트를 조회합니다.
        1. 해당 Entity가 영속성 컨텍스트에 없다면?
            1. DB에서 새롭게 조회합니다.
            2. 조회한 Entity를 영속성 컨텍스트에  저장합니다.
            3. 전달 받은 Entity의 값을 사용하여 병합합니다.
            4. Update SQL이 수행됩니다. (수정)
        2. 만약 DB에서도 없다면 ?
            1. 새롭게 생성한 Entity를 영속성 컨텍스트에 저장합니다.
            2. Insert SQL이 수행됩니다. (저장)

- 삭제(Removed):
remove(entity) : 삭제하기 위해 조회해온 영속 상태의 Entity를 파라미터로 전달받아 삭제 상태로 전환
*/