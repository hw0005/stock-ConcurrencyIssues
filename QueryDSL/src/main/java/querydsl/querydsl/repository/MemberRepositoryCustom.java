package querydsl.querydsl.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import querydsl.querydsl.dto.MemberTeamDto;

public interface MemberRepositoryCustom {
	List<MemberTeamDto> search(MemberSearchCondition condition);
	Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageale);
	Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);

}
