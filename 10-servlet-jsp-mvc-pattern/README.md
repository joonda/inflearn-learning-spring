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

### 3. JSP로 회원 관리 웹 애플리케이션 만들기

- `build.gradle`

```
implementation 'org.apache.tomcat.embed:tomcat-embed-jasper'
implementation 'jakarta.servlet:jakarta.servlet-api'
implementation 'jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api'
implementation 'org.glassfish.web:jakarta.servlet.jsp.jstl'
```

- JSP 관련 의존성 추가.

#### JSP 파일 만들기

- `webapp` > `jsp` > `members`
  - `new-form.jsp`

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<form action="/jsp/members/save.jsp" method="post">
    username: <input type="text" name="username" />
    age:      <input type="text" name="age" />
    <button type="submit">전송</button>
</form>
</body>
</html>
```

- `http://localhost:8080/jsp/members/new-form.jsp` 로 바로 들어갈 수 있다.
- `webapp` 폴더 밑으로 폴더 경로를 따라서 url이 생성된다

- `webapp` > `jsp` > `members`
  - `save.jsp`

```jsp
<%@ page import="hello.servlet.domain.member.Member" %>
<%@ page import="hello.servlet.domain.member.MemberRepository" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    MemberRepository memberRepository = MemberRepository.getInstance();

    String username = request.getParameter("username");
    int age = Integer.parseInt(request.getParameter("age"));

    Member member = new Member(username, age);
    memberRepository.save(member);
%>
<html>
<head>
    <title>Title</title>
</head>
<body>
성공
<ul>
    <li>id=<%=member.getId()%></li>
    <li>username=<%=member.getUsername()%></li>
    <li>age=<%=member.getAge()%></li>
</ul>
<a href="/index.html">go to main</a>
</body>
</html>

```

- `jsp`에서 `request`, `response`는 그냥 예약어로 사용가능

  - `jsp`도 결국 서블릿으로 바뀌기 때문에, 서비스 로직이 그대로 호출

- `webapp` > `jsp`
  - `members.jsp`

```jsp
<%@ page import="java.util.List" %>
<%@ page import="hello.servlet.domain.member.Member" %>
<%@ page import="hello.servlet.domain.member.MemberRepository" %>
<%
    MemberRepository memberRepository = MemberRepository.getInstance();
    List<Member> members = memberRepository.findAll();
%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<a href="/index.html">go to main</a>
<table>
    <thead>
    <th>id</th>
    <th>username</th>
    <th>age</th>
    </thead>
    <tbody>
    <%
        for (Member member : members) {
            out.write("<tr>");
            out.write("<td>" + member.getId() + "</td>");
            out.write("<td>" + member.getUsername() + "</td>");
            out.write("<td>" + member.getAge() + "</td>");
            out.write("</tr>");
        }
    %>
    </tbody>
</table>
</body>
</html>
```

- `<% ~~~ %>`로 자바 코드를 돌릴 수 있다 (for문 등)
  - out은 예약어로, 바로 사용가능 `println()` 개념
- `<%= ~~~ %>`
  - 자바 코드 출력

#### 서블릿과 JSP의 한계

- 서블릿으로 개발 시, `View` 화면을 위한 `HTML`을 만드는 작업이 자바 코드에 섞여 지저분, 복잡
- `JSP`를 사용한 덕분에 `View`를 생성하는 `HTML` 작업을 깔끔 -> 동적 변경 필요한 부분만 자바 코드 적용 하지만 여기서도 문제가 존재
  - 간단한 개발인데도 자바코드 반, 뷰 영역 반을 차지한다 즉, 조금만 규모가 커지면 유지보수가 지옥이 될 것..

#### MVC 패턴 등장

- Model, View, Controller
