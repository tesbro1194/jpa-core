package com.sparta.entity;

import jakarta.persistence.*;

@Entity // JPA가 관리할 수 있는 Entity 클래스 지정, jpa 가 entity 클래스를 객체화할 때 기본 생성자를 사용하니 오버로딩 된 생성자가 있는지 없는지 확인해야함.
@Table(name = "memo") // 매핑할 테이블의 이름을 지정, @Entity , @Table 사용지 (name = "") 안 쓰면 클래스의 이름(Memo)가 디폴트임.

public class Memo {
    // @Id : 테이블의 pk를 지정, 식별자 역할을 한다. Entity 클래스는 반드시 @Id 가 붙은 필드를 가져야함. 즉 필수 에노테이션
    @Id
    // 해당 필드, 즉 칼럼을 auto_increment 하겠다
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // nullable: null 허용 여부
    // unique: 중복 허용 여부 (false 일때 중복 허용), **name = ""** : 매핑할 칼럼의 이름을 넣을 자리
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    // length: 컬럼 길이 지정
    @Column(name = "contents", nullable = false, length = 500)
    private String contents;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}

/* 영속성 컨택스트
DB에서 하나의 트랜잭션에 여러 개의 SQL을 포함하고 있다가 마지막에 영구적으로 변경을 반영하는 것처럼
JPA에서도 영속성 컨텍스트로 관리하고 있는 변경이 발생한 객체들의 정보를 쓰기 지연 저장소에 전부 가지고 있다가
마지막에 SQL을 한번에 DB에 요청해 변경을 반영합니다.*/