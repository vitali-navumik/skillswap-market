# Tests Framework Conventions

This folder contains the Java API test framework for SkillSwap Market based on:

- `JUnit 5`
- `RestAssured`
- `Allure`

## Core style

- Keep tests scenario-focused and short.
- Put transport details into `Api` and `Actions` classes.
- Use `ActionsContainer` and resolvers for authenticated actors.
- Keep reusable business checks in domain assertion classes.
- Prefer fresh test data for business scenarios unless the case is explicitly baseline-read-only.

## API actions

- `*Api` classes build requests.
- `*Actions` classes execute business-level operations.
- Tests should call actions instead of making raw HTTP calls directly.

## Request models

- Request classes follow the current project style: `@Data` + `@Builder` + `@Builder.Default` where needed.
- Do not add Lombok `@NoArgsConstructor` or `@AllArgsConstructor` to request models unless the project explicitly changes this convention later.

## Response deserialization rule

- In API action classes, use `getDataResponse(new TypeRef<>() {})` consistently for response deserialization.
- This rule applies both to generic responses and to single-object DTO responses.
- Do not mix action-class styles between `TypeRef` and `.as(SomeClass.class)` unless the project explicitly changes this convention later.

Example:

```java
@Step("Get user")
public GetUserResponse getUserResponse(UUID publicId) {
    return getUser(publicId)
            .ifOk()
            .getDataResponse(new TypeRef<>() {
            });
}
```

## Test design reminders

- One test should verify one primary business behavior.
- Supporting assertions are welcome, but keep the scenario focused.
- Use actor role and ownership boundaries explicitly in test setup.
- Prefer readable business names in `@Step`, assertions, and test methods.
