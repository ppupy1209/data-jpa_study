package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.Entity;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
         List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

         @Query("select m from Member m where m.username = :username and m.age= :age")
         List<Member> findUser(@Param("username") String username, @Param("age") int age);

         @Query("select m.username from Member m")
         List<String> findUsernameList();

         @Query("select m from Member m where m.username in :names")
         List<Member> findByNames(@Param("names") List<String> names);

         @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
         List<MemberDto> findMemberDto();

         @Query(value = "select m from Member m left join m.team t",
             countQuery = "select count(m) from Member m")
         Page<Member> findByAge(int age, Pageable pageable);

         @Modifying(clearAutomatically = true)
         @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
         int bulkAgePlus(@Param("age") int age);

         @Query("select m from Member m" +
                 " join fetch m.team")
         List<Member> findMemberFetchJoin();

         @Override
         @EntityGraph(attributePaths = {"team"})
         List<Member> findAll();

         @EntityGraph(attributePaths = {"team"})
         @Query("select m from Member m")
         List<Member> findMemberEntityGraph();

         @EntityGraph(attributePaths = {"team"})
         List<Member> findEntityGraphByUsername(String username);

         @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly",value = "true"))
         Member findReadOnlyByUsername(String username);

         @Lock(LockModeType.PESSIMISTIC_WRITE)
         List<Member> findLockByUsername(String username);
}
