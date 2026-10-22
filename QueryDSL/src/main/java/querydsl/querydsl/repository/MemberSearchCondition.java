package querydsl.querydsl.repository;

import lombok.Data;

@Data
public class MemberSearchCondition {
	// 회원명, 팀명, 나이(이상/이하) 검색 조건
    private String username;
    private String teamName;
    private Integer ageGoe; // age >= ageGoe (Greater Than or Equal)
    private Integer ageLoe; // age <= ageLoe (Less Than or Equal)
}
