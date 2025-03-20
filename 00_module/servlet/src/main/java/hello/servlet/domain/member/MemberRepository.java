package hello.servlet.domain.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberRepository {
    // 회원 정보 저장소 (store)
    private static Map<Long, Member> store = new HashMap<>();

    // 회원 추가될 때마다 +1, unique id 부여
    private static long sequence = 0L;

    // instance로 인해 유일한 객체를 생성.
    private static final MemberRepository instance = new MemberRepository();

    // 해당 메서드를 통해 오직 하나의 인스턴스만 사용할 수 있도록 제한
    public static MemberRepository getInstance() {
        return instance;
    }

    // 생성자를 private로 선언, 외부에서 new 키워드로 객체 생성을 제한
    private MemberRepository() {
    }

    public Member save(Member member) {
        member.setId(++sequence);
        store.put(member.getId(), member);
        return member;
    }

    public Member findById(Long id) {
        return store.get(id);
    }

    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    public void clearStore() {
        store.clear();
    }
}
