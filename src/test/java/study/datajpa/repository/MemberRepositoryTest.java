package study.datajpa.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;

    @PersistenceContext
    EntityManager em;




    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        List<Member> members = memberRepository.findAll();

        assertThat(members.size()).isEqualTo(2);

        assertThat(memberRepository.count()).isEqualTo(2);

        memberRepository.delete(member1);
        memberRepository.delete(member2);


        assertThat(memberRepository.count()).isEqualTo(0);

    }

    @Test
    public void findByUsernameAndGreaterThan() {
        Member m1 = new Member("AAA",10);
        Member m2 = new Member("BBB",20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> members = memberRepository.findByUsernameAndAgeGreaterThan("BBB", 15);

        assertThat(members.get(0).getAge()).isEqualTo(20);
    }

    @Test
    public void testQuery() {
        Member m1 = new Member("AAA",10);
        Member m2 = new Member("BBB",20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> members = memberRepository.findUser("BBB",20);

        assertThat(members.size()).isEqualTo(1);
    }

    @Test
    public void findUsernameList() {
        Member m1 = new Member("AAA",10);
        Member m2 = new Member("BBB",20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> members = memberRepository.findUsernameList();

        assertThat(members.get(0)).isEqualTo("AAA");
        assertThat(members.get(1)).isEqualTo("BBB");

    }

    @Test
    public void findMemberDto() {

        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA",10);
        m1.setTeam(team);
        memberRepository.save(m1);


        List<MemberDto> members = memberRepository.findMemberDto();

        for (MemberDto member : members) {
            System.out.println("dto =" + member);
        }
    }

    @Test
    public void findByNames() {
        Member m1 = new Member("AAA",10);
        Member m2 = new Member("BBB",20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> members = memberRepository.findByNames(Arrays.asList("AAA","BBB"));

        for (Member member : members) {
            System.out.println("name=" + member.getUsername());
        }

    }

    @Test
    public void paging() {

        memberRepository.save(new Member("m1",10));
        memberRepository.save(new Member("m2",10));
        memberRepository.save(new Member("m3",10));
        memberRepository.save(new Member("m4",10));
        memberRepository.save(new Member("m5",10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0,3, Sort.by(Sort.Direction.DESC,"username"));

        Page<Member> page = memberRepository.findByAge(age,pageRequest);

        Page<MemberDto> map = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        List<Member> content = page.getContent();


        for (Member member : content) {
            System.out.println("member =" + member);
        }

    }

    @Test
    public void bulkUpdate() {
        memberRepository.save(new Member("m1",10));
        memberRepository.save(new Member("m2",19));
        memberRepository.save(new Member("m3",20));
        memberRepository.save(new Member("m4",21));
        memberRepository.save(new Member("m5",40));

        int resultCount = memberRepository.bulkAgePlus(20);
//        em.flush();
//        em.clear();

        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",10,teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        List<Member> members = memberRepository.findEntityGraphByUsername("member1");

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.team = " + member.getTeam().getName());
        }

    }

}