## 노트

### 1. 프론트 컨트롤러 패턴 소개

- 프론트 컨트롤러 도입 전에는 공통 로직을 각각 따로 작성해줘야하는 번거로움 존재 \* 즉, 반복되는 코드가 증가
  ![before-front-controller](./img/before-front-controller.png)

- 이후, 공통 로직을 프론트 컨트롤러가 처리 후, 각각 필요한 로직은 따로 처리하는 패턴을 만듦
  ![after-front-controller](./img/after-front-controller.png)

- 스프링 웹 MVC와 프론트 컨트롤러
  - 스프링 웹 MVC의 핵심도 `FrontController`
  - 스프링 웹 MVC의 `DispatcherServlet`이 FrontController 패턴으로 구현되어 있음.

### 2. 프론트 컨트롤러 도입 - v1

- `servlet` > `web` > `frontcontroller` > `v1`
  - `ControllerV1`

```java
// ... 생략
public interface ControllerV1 {
    void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
```

- 서블릿과 비슷한 형태의 컨트롤러 인터페이스 도입, 각 컨트롤러들은 해당 인터페이스를 구현하면 된다.
- 프론트 컨트롤러는 이 인터페이스를 호출해서 구현과 관계없이 로직의 일관성을 가져갈 수 있다.

- `servlet` > `web` > `frontcontroller` > `v1` > `controller`
  - `MemberFormControllerV1`

```java
// ... 생략
public class MemberFormControllerV1 implements ControllerV1 {
    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String viewPath = "/WEB-INF/views/new-form.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }
}
```

- 이전 servletmvc 했던 코드와 동일하다.

- `servlet` > `web` > `frontcontroller` > `v1` > `controller`
  - `MemberListControllerV1`

```java
// ... 생략
public class MemberListControllerV1 implements ControllerV1 {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Member> members = memberRepository.findAll();
        request.setAttribute("members", members);
        String viewPath = "/WEB-INF/views/members.jsp";
        request.getRequestDispatcher(viewPath).forward(request, response);
    }
}
```

- `servlet` > `web` > `frontcontroller` > `v1` > `controller`
  - `MemberSaveControllerV1`

```java
// ... 생략
public class MemberSaveControllerV1 implements ControllerV1 {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        request.setAttribute("member", member);

        String viewPath = "/WEB-INF/views/save-result.jsp";
        request.getRequestDispatcher(viewPath).forward(request, response);
    }
}
```

- `servlet` > `web` > `frontcontroller` > `v1`
  - `FrontControllerServletV1`

```java
// ... 생략
@WebServlet(name = "frontControllerServletV1", urlPatterns = "/front-controller/v1/*")
public class FrontControllerServletV1 extends HttpServlet {

    private Map<String, ControllerV1> controllerMap = new HashMap<>();

    public FrontControllerServletV1() {
        controllerMap.put("/front-controller/v1/members/new-form", new MemberFormControllerV1());
        controllerMap.put("/front-controller/v1/members/save", new MemberSaveControllerV1());
        controllerMap.put("/front-controller/v1/members", new MemberListControllerV1());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // uri 파라미터를 받아올 수 있다.
        String requestURI = request.getRequestURI();

        ControllerV1 controller = controllerMap.get(requestURI);

        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        controller.process(request, response);
    }
}
```

- `urlPatterns`에서 `*`로 `/front-controller/v1`을 포함한 하위 모든 요청은 해당 서블릿에서 받아들인다.
- 해당 코드에서 `HashMap`을 사용, controller들의 uri를 반환 받아서 각 Map에 해당하는 uri가 들어오면 그 해당 되는 value인 controller가 반환되는 방식으로 진행
- 해당 `controller`의 `process`를 호출 `request`, `response` 파라미터로 받아서 진행
  - 만약 `controller`가 `null` 이라면, `404 Error`

### 3. View 분리 - v2

- 모든 컨트롤러에서 뷰로 이동하는 부분에 중복이 존재, 깔끔하지 않음.

```java
String viewPath = "/WEB-INF/views/new-form.jsp";
RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
dispatcher.forward(request, response);
```

- 이를 해결하기 위해 별도로 뷰를 처리하는 객체를 만든다.

- `servlet` > `web` > `frontcontroller`
  - `MyView`

```java
public class MyView {
    private String viewPath;

    public MyView(String viewPath) {
        this.viewPath = viewPath;
    }

    public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }
}
```

- 기존에는 컨트롤러에서 직접 `RequestDispatcher`를 사용해 JSP 파일로 포워딩
- `MyView`클래스를 도입해 렌더링 로직을 한 곳에서 일괄적으로 처리

- `servlet` > `web` > `frontcontroller` > `v2`
  - `ControllerV2`

```java
// ... 생략
public interface ControllerV2 {
    MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
```

- 이후 `interface`를 만들때, 기존의 `void` 타입 반환에서 `MyView` 반환으로 변경

* `servlet` > `web` > `frontcontroller` > `v2` > `controller`
  - `MemberFormControllerV2`

```java
// ... 생략
public class MemberFormControllerV2 implements ControllerV2 {

    @Override
    public MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        return new MyView("/WEB-INF/views/new-form.jsp");
    }
}
```

- `servlet` > `web` > `frontcontroller` > `v2` > `controller`
  - `MemberListControllerV2`

```java
// ... 생략
public class MemberListControllerV2 implements ControllerV2 {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Member> members = memberRepository.findAll();
        request.setAttribute("members", members);
        return new MyView("/WEB-INF/views/members.jsp");
    }
}
```

- `servlet` > `web` > `frontcontroller` > `v2` > `controller`
  - `MemberSaveControllerV2`

```java
// ... 생략
public class MemberSaveControllerV2 implements ControllerV2 {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public MyView process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        request.setAttribute("member", member);

        return new MyView("/WEB-INF/views/save-result.jsp");

    }
}
```

- `servlet` > `web` > `frontcontroller` > `v2`
  - `FrontControllerServletV2`

```java
// ... 생략
@WebServlet(name = "frontControllerServletV2", urlPatterns = "/front-controller/v2/*")
public class FrontControllerServletV2 extends HttpServlet {

    private Map<String, ControllerV2> controllerMap = new HashMap<>();

    public FrontControllerServletV2() {
        controllerMap.put("/front-controller/v2/members/new-form", new MemberFormControllerV2());
        controllerMap.put("/front-controller/v2/members/save", new MemberSaveControllerV2());
        controllerMap.put("/front-controller/v2/members", new MemberListControllerV2());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // uri 파라미터를 받아올 수 있다.
        String requestURI = request.getRequestURI();

        ControllerV2 controller = controllerMap.get(requestURI);

        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // new MyView("/WEB-INF/views/new-form.jsp");
        MyView view = controller.process(request, response);
        view.render(request, response);
    }
}
```

- interface에서 return을 MyView로 통일한다 (기존에는 void)
- 각각 Controller에서 return에 새로운 MyView 객체를 반환한다 (`jsp` 경로를 파라미터로 받는다 (`viewPath`))

* 이후 `FrontControllerServletV2`에서 `MyView`로 반환된 객체 -> `view.render` 실행

### 4. Model 추가 - v3

- **서블릿 종속성 제거**

  - `request` 객체를 `Model`로 사용하는 대신, 별도의 `Model` 객체를 만들어서 반환하면 된다.
  - **우리가 구현하는 컨트롤러가 서블릿 기술을 전혀 사용하지 않도록 변경하는 것이 목적**
  - 구현 코드 단순화, 테스트 코드 작성이 쉽다.

- #### **뷰 이름 중복 제거**

  - `/WEB-INF/views/new-form.jsp` -> `new-form`
  - `/WEB-INF/views/save-result.jsp` -> `save-result`
  - `/WEB-INF/views/members.jsp` -> `members`

- `servlet` > `web` > `frontcontroller`
  - `ModelView`

```java
// ... 생략
public class ModelView {
    private String viewName;
    private Map<String, Object> model = new HashMap<>();

    public ModelView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, Object> getModel() {
        return model;
    }

    public void setModel(Map<String, Object> model) {
        this.model = model;
    }
}
```

- 논리적 View 이름 저장 (`ViewName`)
- 모델 데이터 저장 (`model`)

* `servlet` > `web` > `frontcontroller` > `v3`
  - `ControllerV3`

```java
// ... 생략
public interface ControllerV3 {
    ModelView process(Map<String, String> paramMap);
}
```

- `paramMap`을 받아서 `ModelView` 반환하도록 설계

* `servlet` > `web` > `frontcontroller` > `v3` > `controller`
  - `MemberFormControllerV3`

```java
// ... 생략
public class MemberFormControllerV3 implements ControllerV3 {

    @Override
    public ModelView process(Map<String, String> paramMap) {
        return new ModelView("new-form");
    }
}
```

- `new-form` (uri 파라미터) 를 받은 `ModelView` 객체를 반환

* `servlet` > `web` > `frontcontroller` > `v3` > `controller`
  - `MemberSaveControllerV3`

```java
// ... 생략
public class MemberSaveControllerV3 implements ControllerV3 {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public ModelView process(Map<String, String> paramMap) {
        String username = paramMap.get("username");
        int age = Integer.parseInt(paramMap.get("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        ModelView mv = new ModelView("save-result");
        mv.getModel().put("member", member);

        return mv;
    }
}
```

- 논리 View 이름인 `save-result`를 파라미터로 받아서 mv에 저장
- ModelView를 생성하여 논리 뷰 이름과 모델 데이터를 저장한다.

* `servlet` > `web` > `frontcontroller` > `v3` > `controller`
  - `MemberListControllerV3`

```java
// ... 생략
public class MemberListControllerV3 implements ControllerV3 {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public ModelView process(Map<String, String> paramMap) {
        List<Member> members = memberRepository.findAll();
        ModelView mv = new ModelView("members");
        mv.getModel().put("members", members);
        return mv;
    }
}

```

- `servlet` > `web` > `frontcontroller` > `v3`
  - `FrontControllerServletV3`

```java
// ... 생략
@WebServlet(name = "frontControllerServletV3", urlPatterns = "/front-controller/v3/*")
public class FrontControllerServletV3 extends HttpServlet {

    private Map<String, ControllerV3> controllerMap = new HashMap<>();

    public FrontControllerServletV3() {
        controllerMap.put("/front-controller/v3/members/new-form", new MemberFormControllerV3());
        controllerMap.put("/front-controller/v3/members/save", new MemberSaveControllerV3());
        controllerMap.put("/front-controller/v3/members", new MemberListControllerV3());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // uri 파라미터를 받아올 수 있다.
        String requestURI = request.getRequestURI();

        ControllerV3 controller = controllerMap.get(requestURI);

        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // paramMap
        Map<String, String> paramMap = createParamMap(request);
        ModelView mv = controller.process(paramMap);

        String viewName = mv.getViewName(); // 논리이름

        MyView view = viewResolver(viewName);

        view.render(mv.getModel(), request, response);

    }

    private static MyView viewResolver(String viewName) {
        return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    }

    private static Map<String, String> createParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        request.getParameterNames().asIterator()
                .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;
    }
}
```

- `/front-controller/v3/*`로 들어오는 모든 요청 처리
- `request.getRequestURI()`로 현재 uri를 가져온다음, `controllerMap`에 등록된 uri에서 컨트롤러를 찾는다.
  - `/front-controller/v3/members/save` -> `MemberSaveControllerV3()`
- `createParamMap()` > `request.getParameterNames()`를 반복하면서 모든 요청 파라미터를 Map 형태에 저장
  - `controller`는 `HttpServletRequest` 없이 `paramMap`을 통해 데이터를 받을 수 있음.
- `controller.process()` 컨트롤러를 실행하여 논리적 뷰 이름 + 모델 데이터를 담은 ModelView 객체 반환
- `viewResolver()`로 실제 JSP 경로 반환
- `view.render()`

#### 추가

- `servlet` > `web` > `frontcontroller`
  - `MyView`

```java
// ...생략
public class MyView {
    private String viewPath;

    public MyView(String viewPath) {
        this.viewPath = viewPath;
    }

    // 기존 render 방식
    public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }

    // 모델 데이터를 전달하는 render 방식 추가
    public void render(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        modelToRequestAttribute(model, request);
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }

    private static void modelToRequestAttribute(Map<String, Object> model, HttpServletRequest request) {
        model.forEach((key, value) -> request.setAttribute(key, value));
    }
}
```

### 5. 단순하고 실용적인 컨트롤러 - v4

- 기본적인 구조는 V3와 같으며, 대신 컨트롤러가 `ModelView`를 반환하지 않고, `ViewName`만 반환

- `servlet` > `web` > `frontcontroller` > `v4`
  - `ControllerV4`

```java
package hello.servlet.web.frontcontroller.v4;

import java.util.Map;

public interface ControllerV4 {

    /**
     * @param paramMap
     * @param model
     * @return viewName
     */

    String process(Map<String, String> paramMap, Map<String, Object> model);
}
```

- `servlet` > `web` > `frontcontroller` > `v4` > `controller`
  - `MemberFormControllerV4`

```java
// ... 생략
public class MemberFormControllerV4 implements ControllerV4 {

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        return "new-form";
    }
}
```

- `servlet` > `web` > `frontcontroller` > `v4` > `controller`
  - `MemberFormControllerV4`

```java
// ... 생략
public class MemberSaveControllerV4 implements ControllerV4 {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        String username = paramMap.get("username");
        int age = Integer.parseInt(paramMap.get("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        model.put("member", member);

        return "save-result";
    }
}

```

- `servlet` > `web` > `frontcontroller` > `v4` > `controller`
  - `MemberListControllerV4`

```java
// ... 생략
public class MemberListControllerV4 implements ControllerV4 {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        List<Member> members = memberRepository.findAll();
        model.put("members", members);

        return "members";
    }
}
```

- `servlet` > `web` > `frontcontroller` > `v4`
  - `FrontControllerServletV4`

```java
@WebServlet(name = "frontControllerServletV4", urlPatterns = "/front-controller/v4/*")
public class FrontControllerServletV4 extends HttpServlet {

    private Map<String, ControllerV4> controllerMap = new HashMap<>();

    public FrontControllerServletV4() {
        controllerMap.put("/front-controller/v4/members/new-form", new MemberFormControllerV4());
        controllerMap.put("/front-controller/v4/members/save", new MemberSaveControllerV4());
        controllerMap.put("/front-controller/v4/members", new MemberListControllerV4());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // uri 파라미터를 받아올 수 있다.
        String requestURI = request.getRequestURI();

        ControllerV4 controller = controllerMap.get(requestURI);

        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // paramMap
        Map<String, String> paramMap = createParamMap(request);
        Map<String, Object> model = new HashMap<>();
        String viewName = controller.process(paramMap, model);
        MyView view = viewResolver(viewName);

        view.render(model, request, response);

    }

    private static MyView viewResolver(String viewName) {
        return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    }

    private static Map<String, String> createParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        request.getParameterNames().asIterator()
                .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;
    }
}
```

- 기존 구조에서 모델을 파라미터로 넘기고 뷰의 논리 이름만 반환
  - `FrontController`에서 model까지 넘겨줌
- 각 컨트롤러 입장에서는 viewName만 반환시켜주면 된다.

### 6. 유연한 컨트롤러1 - v5

- `ControllerV3` 또는 `ControllerV4` 방식으로 개발하고 싶을 수 있다.

#### 어댑터 패턴

- 지금까지 개발한 프론트 컨트롤러는 한가지 방식의 컨트롤러 인터페이스만 사용 가능
  - ControllerV3, ControllerV4, ...
  - 서로 호환은 불가능
- 어댑터 패턴을 사용해서 프론트 컨트롤러가 다양한 방식의 컨트롤러를 처리할 수 있도록 변경

- `servlet` > `web` > `frontcontroller` > `v5`
  - `MyHandlerAdapter`

```java
// ... 생략
public interface MyHandlerAdapter {

    boolean supports(Object handler);

    ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException;
}
```

- `servlet` > `web` > `frontcontroller` > `v5` > `adapter`
  - `ControllerV3HandlerAdapter`

```java
// ... 생략
public class ControllerV3HandlerAdapter implements MyHandlerAdapter {
    @Override
    public boolean supports(Object handler) {
        return (handler instanceof ControllerV3);
    }

    @Override
    public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException {
        ControllerV3 controller = (ControllerV3) handler;

        Map<String, String> paramMap = createParamMap(request);

        ModelView mv = controller.process(paramMap);
        return mv;
    }

    private static Map<String, String> createParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        request.getParameterNames().asIterator()
                .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;
    }
}
```

- `servlet` > `web` > `frontcontroller` > `v5`
  - `FrontControllerServletV5`

```java
@WebServlet(name = "frontControllerServletV5", urlPatterns = "/front-controller/v5/*")
public class FrontControllerServletV5 extends HttpServlet {
    private final Map<String, Object> handlerMappingMap = new HashMap<>();
    private final List<MyHandlerAdapter> handlerAdapters = new ArrayList<>();

    public FrontControllerServletV5() {
        initHandlerMappingMap();
        initHandlerAdapters();
    }

    private void initHandlerMappingMap() {
        handlerMappingMap.put("/front-controller/v5/v3/members/new-form", new MemberFormControllerV3());
        handlerMappingMap.put("/front-controller/v5/v3/members/save", new MemberSaveControllerV3());
        handlerMappingMap.put("/front-controller/v5/v3/members", new MemberListControllerV3());
    }

    private void initHandlerAdapters() {
        handlerAdapters.add(new ControllerV3HandlerAdapter());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Object handler = getHandler(request);
        if (handler == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        MyHandlerAdapter adapter = getHandlerAdapter(handler);

        ModelView mv = adapter.handle(request, response, handler);

        String viewName = mv.getViewName();
        MyView view = viewResolver(viewName);

        view.render(mv.getModel(), request, response);
    }

    private Object getHandler(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return handlerMappingMap.get(requestURI);
    }

    private MyHandlerAdapter getHandlerAdapter(Object handler) {
        for (MyHandlerAdapter adapter : handlerAdapters) {
            if (adapter.supports(handler)) { // true / false 반환
                return adapter;
            }
        }
        throw new IllegalArgumentException("no adapter for handler : " + handler);
    }

    private static MyView viewResolver(String viewName) {
        return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    }
}
```
