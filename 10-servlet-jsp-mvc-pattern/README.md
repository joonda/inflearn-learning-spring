## 노트

### 1. 회원 관리 웹 애플리케이션 요구사항

`회원정보`
이름 : `username`
나이 : `age`

```java
// ... 생략
public class MemberRepository {
    // 회원 정보 저장소 (store)
    private static Map<Long, Member> store = new HashMap<>();

    // 회원 추가될 때마다 +1, unique id 부여
    private static long sequence = 0L;

    // instance로 인해 유일한 객체를 생성.
    private static final MemberRepository instance = new MemberRepository();

    // 해당 메서드를 통해 오직 하나의 인스턴스만 사용할 수 있도록 제한
    private static MemberRepository getInstance() {
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
```

- `instance`로 유일한 객체를 지정하여 생성
  - `getInstance()` 메서드로 오직 하나의 인스턴스만 사용할 수 있도록 제한
  - 기본 생성자 `private MemberRepository() {}`를 선언, 외부에서 new 키워드로 객체 생성을 제한.

```java
// ... 생략
class MemberRepositoryTest {

    MemberRepository memberRepository = MemberRepository.getInstance();

    @AfterEach
    void afterEach() {
        memberRepository.clearStore();
    }

    @Test
    void save() {
        // given
        Member member = new Member("Hello", 20);

        // when
        Member savedMember = memberRepository.save(member);

        // then
        Member findMember = memberRepository.findById(savedMember.getId());
        assertThat(findMember).isEqualTo(savedMember);

    }

    @Test
    void findAll() {
        // given
        Member member1 = new Member("member1", 20);
        Member member2 = new Member("member2", 30);

        memberRepository.save(member1);
        memberRepository.save(member2);
        // when
        List<Member> result = memberRepository.findAll();

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).contains(member1, member2);
    }
}
```

- Test Code 작성, 익숙해지자.

### 2. 서블릿으로 회원 관리 웹 애플리케이션 만들기

- `src` > `main` > `java` > `hello` > `servlet` > `web` > `servlet`
  - `MemberFormServlet`

```java
// ... 생략
@WebServlet(name = "memberFormServlet", urlPatterns = "/servlet/members/new-form")
public class MemberFormServlet extends HttpServlet {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");

        PrintWriter w = response.getWriter();
        w.write("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Title</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<form action=\"/servlet/members/save\" method=\"post\">\n" +
                "    username: <input type=\"text\" name=\"username\" />\n" +
                "    age:      <input type=\"text\" name=\"age\" />\n" +
                "    <button type=\"submit\">전송</button>\n" +
                "</form>\n" +
                "</body>\n" +
                "</html>\n");
    }
}

```

- `src` > `main` > `java` > `hello` > `servlet` > `web` > `servlet`
  - `MemberSaveServlet`

```java
// ... 생략
@WebServlet(name = "memberSaveServlet", urlPatterns = "/servlet/members/save")
public class MemberSaveServlet extends HttpServlet {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");
        PrintWriter w = response.getWriter();
        w.write("<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "</head>\n" +
                "<body>\n" +
                "성공\n" +
                "<ul>\n" +
                "    <li>id="+member.getId()+"</li>\n" +
                "    <li>username="+member.getUsername()+"</li>\n" +
                "    <li>age="+member.getAge()+"</li>\n" +
                "</ul>\n" +
                "<a href=\"/index.html\">메인</a>\n" +
                "</body>\n" +
                "</html>");
    }
}
```

- `src` > `main` > `java` > `hello` > `servlet` > `web` > `servlet`
  - `MemberListServlet`

```java
// ... 생략
@WebServlet(name = "memberListServlet", urlPatterns = "/servlet/members")
public class MemberListServlet extends HttpServlet {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");

        List<Member> members = memberRepository.findAll();

        PrintWriter w = response.getWriter();

        w.write("<html>");
        w.write("<head>");
        w.write("    <meta charset=\"UTF-8\">");
        w.write("    <title>Title</title>");
        w.write("</head>");
        w.write("<body>");
        w.write("<a href=\"/index.html\">메인</a>");
        w.write("<table>");
        w.write("    <thead>");
        w.write("    <th>id</th>");
        w.write("    <th>username</th>");
        w.write("    <th>age</th>");
        w.write("    </thead>");
        w.write("    <tbody>");

        for (Member member : members) {
        w.write("    <tr>");
        w.write("        <td>" + member.getId() + "</td>");
        w.write("        <td>" + member.getUsername() + "</td>");
        w.write("        <td>" + member.getAge() + "</td>");
        w.write("    </tr>");
    }
        w.write("    </tbody>");
        w.write("</table>");
        w.write("</body>");
        w.write("</html>");
    }
}
```

- `memberFormServlet`의 `form` 태그의 `action`을 통해 `MemberSaveServlet`로 이동이 되어 `MemberRepository`에 회원 정보가 저장이 된다
  - 이후 저장된 리스트를 확인하기 위해, `MemberListServlet`에서 목록을 확인할 수 있다.
- response, request 등으로 주고받는 것은 좋은데, html을 일일히 손수 짜야한다는 것이 매우 에러.
  - 템플릿 엔진을 사용하는 것이 좋음.
    - `JSP`, `Thymeleaf`, `Freemarker`, `Velocity` 등이 있다.
