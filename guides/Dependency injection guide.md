# Dependency injection in Multimatum instrumented tests

## Concerns
Android <b>instrumented</b> tests using dependency injection

## Goal
Perform DI on an activity to inject a dependency for the app and another one for tests

## Uses
* Hilt for dependency injection
* Mockito for mocking; I (Valentin) am not sure that this is possible with Mockk

## Documentation
On dependency injection with Hilt: https://developer.android.com/training/dependency-injection/hilt-android <p>
On tests using dependency injection: https://developer.android.com/training/dependency-injection/hilt-testing

## Guide
MainSettingsActivity is used as an example
1. Annotate the activity with `@AndroidEntryPoint`
```Kotlin
@AndroidEntryPoint
class MainSettingsActivity : AppCompatActivity() { ... }
```
2. Declare the class member that you want to inject and annotate it with `@Inject`. It must a public `lateinit var`.
```Kotlin
@Inject lateinit var preferences: SharedPreferences
```
3. Go to the `DependenciesProvider` object and add a function that returns the instance which must be injected. Annotate the 
function with `@Provides`. This function will be used when actually running the app (for the tests the goal is to be able to
use another provider).
```Kotlin
@Provides fun provideSharedPreferences(@ApplicationContext applicationContext: Context): SharedPreferences =
        applicationContext.getSharedPreferences(SHARED_PREF_ID, MODE_PRIVATE)
```
The `applicationContext` argument might not be useful depending on what you want to inject and is not mandatory.
Once you did that, ![dependency-icon](./dependency-icon.jpg) should appear on the left of the function (in Android Studio),
and clicking on it should lead to the `@Inject` class member that you created.

4. Create the test class and annotate it as follows:
```Kotlin
@UninstallModules(DependenciesProvider::class)
@HiltAndroidTest
class MainSettingsActivityTest { ... }
```
5. In the test class add this:
```Kotlin
@get:Rule
var hiltRule = HiltAndroidRule(this)
@Before fun init() { hiltRule.inject() }
```
6. Create an object that will provide dependencies to the tests and annotate it as follows:
```Kotlin
@Module
@InstallIn(SingletonComponent::class)
object TestDependenciesProvider { ... }
```
7. As in point 3, create a function that returns the instance that you want to inject for tests and annotate the function with `@Provides`.
If you want to inject a mock, declare it in an object so that both this function and the tests can access it.

