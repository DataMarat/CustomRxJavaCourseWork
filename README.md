# Курсовая работа: Реализация собственной библиотеки RxJava

Данная курсовая работа посвящена реализации собственной минималистичной версии библиотеки RxJava — инструмента для реактивного программирования на языке Java. Цель проекта — создать систему, основанную на паттерне «Наблюдатель» (Observer pattern), способную обрабатывать потоки данных, управлять многопоточностью и обрабатывать ошибки в декларативном стиле.

Реактивный подход позволяет организовывать асинхронные потоки данных и событий в виде цепочек преобразований, где обработка происходит только по мере поступления новых элементов. Это делает код более понятным, модульным и пригодным для масштабирования в многопоточном окружении.

## Структура проекта

```
CustomRxJavaCourseWork/
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── core/
│   │       │   ├── Observable.java
│   │       │   ├── Observer.java
│   │       │   ├── Disposable.java
│   │       │   └── DisposableObserver.java
│   │       └── schedulers/
│   │           ├── Scheduler.java
│   │           ├── IOThreadScheduler.java
│   │           ├── ComputationScheduler.java
│   │           └── SingleThreadScheduler.java
│   └── test/
│       └── java/
│           └── test/
│               ├── ObservableTest.java
│               ├── SchedulerTest.java
│               └── OperatorTest.java
├── README.md
└── pom.xml
```

Проект организован по стандартной структуре Maven. Исходный код и тесты разделены по пакетам.
Компоненты ядра (`Observable`, `Observer` и операторы) находятся в пакете `core`. 
Компоненты управления потоками (`Schedulers`) — в пакете `schedulers`. Юнит-тесты — в `test/java/test`.

## Архитектура системы

Проект реализует минимальную версию библиотеки реактивного программирования, вдохновлённую RxJava. Архитектура построена вокруг паттерна «Наблюдатель» (Observer pattern) и поддерживает основные элементы реактивного потока: подписку, обработку событий, асинхронное выполнение, операторы преобразования и управление подписками.

### Основные компоненты:

- **Observable<T>**  
  Представляет источник данных или событий. Позволяет создавать потоки с помощью метода `create()` и обрабатывать данные через цепочку операторов (`map`, `filter`, `flatMap`, `observeOn`, `subscribeOn`).

- **Observer<T>**  
  Интерфейс подписчика, содержащий методы:
    - `onNext(T item)` — обработка следующего элемента потока;
    - `onError(Throwable)` — обработка ошибки;
    - `onComplete()` — сигнал об окончании потока.

- **Disposable / DisposableObserver**  
  Интерфейс и абстракция для отмены подписки. Позволяет прерывать получение данных на определённой стадии.

- **Операторы**  
  Методы, применяемые к Observable, возвращающие новый Observable:
    - `map(Function<T, R>)` — преобразование элементов;
    - `filter(Predicate<T>)` — фильтрация элементов;
    - `flatMap(Function<T, Observable<R>>)` — разворачивание вложенных Observable;
    - `subscribeOn(Scheduler)` и `observeOn(Scheduler)` — переключение потоков выполнения.

- **Schedulers**  
  Механизм управления потоками:
    - `Scheduler` — интерфейс с методом `execute(Runnable)`;
    - `IOThreadScheduler` — для IO-задач (cached thread pool);
    - `ComputationScheduler` — для CPU-задач (fixed thread pool);
    - `SingleThreadScheduler` — для последовательных задач (один поток).

Взаимодействие компонентов построено по принципу ленивой обработки: данные обрабатываются только при подписке. Потоки и ресурсы управляются через Scheduler'ы и Disposable-объекты.

## Принципы работы Schedulers и их применение

Schedulers используются для управления выполнением задач в многопоточной среде. Они позволяют отделить генерацию данных (subscribe) от их обработки (observe) и выполнять эти этапы в различных потоках, что критично для асинхронных и неблокирующих приложений.

### Общий принцип

Schedulers разделяют:

- Поток, в котором происходит подписка (subscribe).
- Поток, в котором обрабатываются события (observe).

Это позволяет эффективно распределять нагрузку и избегать блокировки основного потока исполнения.

### Интерфейс Scheduler

Все планировщики реализуют единый интерфейс с методом запуска задачи. В зависимости от реализации, задача будет выполнена либо в пуле потоков, либо в единственном потоке.
```java
public interface Scheduler {
    void execute(Runnable task);
}
```
Каждая реализация Scheduler запускает переданные задачи по-своему, с помощью различных типов потоков.

### Реализации

- **IOThreadScheduler**  
  Использует кэшируемый пул потоков. Хорошо подходит для операций с переменной продолжительностью, таких как сетевые вызовы и доступ к диску.

- **ComputationScheduler**  
  Основывается на фиксированном пуле потоков, равном количеству доступных процессорных ядер. Применяется для задач, связанных с интенсивными вычислениями.

- **SingleThreadScheduler**  
  Обеспечивает последовательное выполнение всех задач в одном потоке. Полезен там, где важен порядок исполнения, например при логировании или обновлении пользовательского интерфейса.

### Применение

- Метод `subscribeOn` задаёт Scheduler, на котором происходит подписка на источник данных, т.е. генерация событий.
- Метод `observeOn` задаёт Scheduler, в котором будет происходить обработка событий, получаемых наблюдателем.

Благодаря этому можно, например, получать данные в одном потоке, а обрабатывать — в другом, обеспечивая эффективное и безопасное параллельное выполнение.

Пример использования

```java
Observable.create(emitter -> {
    emitter.onNext("start");
    emitter.onComplete();
})
.subscribeOn(new IOThreadScheduler())
.observeOn(new SingleThreadScheduler())
.subscribe(...);
```
В этом примере генерация данных происходит в пуле потоков (IO), а обработка — в одном потоке (например, для обновления UI).

## Описание процесса тестирования

Проект сопровождается полным набором юнит-тестов, которые охватывают основные функциональные возможности системы. Тесты написаны с использованием JUnit 5 и расположены в пакете `test`.

### Структура тестов

- **ObservableTest**  
  Проверяет базовую функциональность Observable и операторов `map` и `filter`.  
  Тестируются корректная эмиссия элементов, завершение потока и отсутствие ошибок при штатной работе.

- **OperatorTest**  
  Содержит тесты для оператора `flatMap` и механизма отмены подписки (`DisposableObserver`).  
  Проверяется корректная трансформация вложенных Observable, а также прекращение получения элементов после вызова `dispose()`.

- **SchedulerTest**  
  Проверяет работу `subscribeOn` и `observeOn` с различными реализациями `Scheduler`.  
  Тестируются:
    - выполнение подписки и обработки в отдельных потоках;
    - соответствие потоков ожидания и обработки;
    - последовательное выполнение задач в `SingleThreadScheduler`.

### Цели тестирования

- Убедиться в корректной логике реактивных операторов.
- Проверить обработку ошибок и завершения потоков.
- Оценить поведение в многопоточной среде.
- Подтвердить работоспособность отмены подписки.

Каждый тест изолирован, стабилен и воспроизводим. Тесты демонстрируют ожидаемое поведение системы в различных условиях и могут использоваться как примеры применения компонентов библиотеки.

## Примеры использования реализованной библиотеки

Ниже описаны сценарии, демонстрирующие применение основных возможностей реализованной реактивной библиотеки.

### Подписка на поток данных

Observable можно создать с помощью метода `create()`, где описывается логика генерации событий. После этого можно подписаться на поток с помощью `subscribe()` и реализовать методы `onNext`, `onError`, `onComplete`.

### Преобразование и фильтрация данных

С помощью оператора `map` можно преобразовывать элементы потока. Оператор `filter` позволяет пропускать только те элементы, которые удовлетворяют заданному условию.

### Обработка асинхронно

Для переключения потоков используются методы `subscribeOn` и `observeOn`. Это позволяет, например, загружать данные в одном потоке, а обрабатывать их в другом, не блокируя основной поток приложения.

### Комбинирование потоков

Оператор `flatMap` позволяет на каждый элемент исходного потока возвращать новый Observable и объединять все вложенные потоки в один. Это полезно, например, для асинхронных запросов с последующей обработкой результатов.

### Отмена подписки

Если необходимо прекратить получение данных (например, при достижении определённого условия), можно использовать `DisposableObserver`, который предоставляет метод `dispose()`. Это позволяет эффективно управлять ресурсами и прекращать обработку, когда она становится неактуальной.

## Инструкция по запуску проекта и тестов

### Требования

- Java 17
- Maven 3.8+
- Совместимая IDE (например, IntelliJ IDEA или VS Code)

### Сборка проекта

Откройте терминал в корне проекта и выполните:

```bash
mvn clean install
```

Проект будет собран, зависимости загружены.

### Запуск тестов

Для запуска всех юнит-тестов:

```bash
mvn test
```

Тесты находятся в папке `src/test/java/test/` и запускаются автоматически при сборке. Вывод в консоли покажет статус каждого теста.

### Проверка функциональности

1. Все основные возможности (`create`, `map`, `filter`, `flatMap`, `Schedulers`, `Disposable`) протестированы автоматически.
2. Для ручной проверки можно создать отдельный класс `ExampleUsage` и выполнить любые сценарии с консольным выводом.
3. Код легко расширяется за счёт универсального интерфейса `Observable` и совместимости с лямбда-выражениями.

### Демонстрационный пример

Для ручной проверки работы всех компонентов в проекте реализован демонстрационный класс `Main.java`. Он находится по пути:
```bash
src/main/java/Main.java
```

Пример вывода программы:

```bash
[main] INFO Main - Example 1: flatMap
[main] INFO Main - [flatMap] Item: 10
[main] INFO Main - [flatMap] Item: 20
[main] INFO Main - [flatMap] Item: 20
[main] INFO Main - [flatMap] Item: 40
[main] INFO Main - [flatMap] Item: 30
[main] INFO Main - [flatMap] Item: 60
[main] INFO Main - [flatMap] Completed
[main] INFO Main - Example 2: Error handling
[main] INFO Main - [error] Received: A
[main] INFO Main - [error] Received: B
[main] WARN Main - [error] Handled: Simulated error
[main] INFO Main - Example 3: Disposable
[pool-3-thread-1] INFO Main - [infinite] 0
[pool-3-thread-1] INFO Main - [infinite] 1
[pool-3-thread-1] INFO Main - [infinite] 2
[pool-3-thread-1] INFO Main - [infinite] 3
[pool-3-thread-1] INFO Main - [infinite] 4
[pool-3-thread-1] INFO Main - [infinite] 5
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 6
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 7
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 8
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 9
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 10
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 11
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 12
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 13
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 14
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 15
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 16
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 17
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 18
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[main] INFO Main - Done.
[pool-3-thread-1] INFO Main - [infinite] 19
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 20
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 21
[pool-3-thread-1] INFO Main - [infinite] Disposed!
[pool-3-thread-1] INFO Main - [infinite] 22
[pool-3-thread-1] INFO Main - [infinite] Disposed!
```

В нём представлены примеры использования следующих возможностей библиотеки:

- оператор `flatMap`;
- обработка ошибок через `onError`;
- управление подпиской с помощью `DisposableObserver`;
- переключение потоков выполнения с использованием `Schedulers`.

Вывод производится через логирование (SLF4J), что позволяет удобно отслеживать последовательность событий и и поток исполнения.

### Возможные ошибки

- Убедитесь, что используется именно Java 17.
- При запуске в IDE укажите верную конфигурацию Maven JDK и пути исходного кода (`src/main/java` и `src/test/java`).
