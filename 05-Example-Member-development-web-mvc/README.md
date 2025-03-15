## 노트

### 1. 회원 웹 기능 - 홈 화면 추가
`HomeController`

```java
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

}
```

`main` > `resources` > `templates`
`home.html`

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<body>
<div class="container">
    <div>
        <h1>Hello Spring</h1>
        <p>회원 기능</p>
        <p>
            <a href="/members/new">회원 가입</a>
            <a href="/members">회원 목록</a>
        </p> </div>
</div> <!-- /container -->
</body>
</html>
```

### 2. 회원 웹 기능 - 등록

`MemberController`
```java
@Controller
public class MemberController {

    private final MemberService memberService;

    // 의존관계 주입
    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members/new")
    public String createForm() {
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    public String create(MemberForm memberForm) {
        Member member = new Member();
        member.setName(memberForm.getName());

        memberService.join(member);

        return "redirect:/";
    }
}
```
* `@GetMapping`을 활용하여 `members/new`를 지정
    * `templates/createMemberForm.html` 파일을 불러온다
* MemberForm에서 등록을 누르면, home (`/`) 으로 redirect

`main` > `resources` > `templates` > `members`
`createMemberForm.html`
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<body>
<div class="container">
    <form action="/members/new" method="post"> <div class="form-group">
        <label for="name">이름</label>
        <input type="text" id="name" name="name" placeholder="이름을 입력하세요">
    </div>
        <button type="submit">등록</button>
    </form>
</div> <!-- /container -->
</body>
</html>
```
* action은 데이터를 전달할 경로, method는 `post` (회원 등록)
* input 태그에 `name`을 활용하여 데이터 키값으로 받는다.

`MemberForm`
```java
public class MemberForm {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

### 3. 회원 웹 기능 - 조회
```java
@Controller
public class MemberController {

    private final MemberService memberService;

    // 의존관계 주입
    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // ... 생략
    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }
}
```
* model에서 List 형식의 members를 받아서 전달
* `memberList.html`을 `/members` 엔드포인트 경로에 뿌려준다

`main` > `resources` > `templates` > `members`
`memberList.html`
```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<body>
<div class="container">
    <div>
        <table>
            <thead>
            <tr>
                <th>#</th>
                <th>이름</th>
            </tr>
            </thead>
            <tbody>
            <tr th:each="member : ${members}">
                <td th:text="${member.id}"></td>
                <td th:text="${member.name}"></td>
            </tr>
            </tbody>
        </table>
    </div>
</div> <!-- /container -->
</body>
</html>
```
* `List`로 받아온 `members`를 `each` 문법을 활용하여 각각 뿌려준다