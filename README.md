# E-commerce API

Spring Boot 4 / Java 21 backend for an e-commerce platform.

## Design decisions

### SQL (MySQL) instead of NoSQL
E-commerce data is relational by nature: products, orders, customers, payments and stock all reference each other, and things like placing an order and decrementing stock need to happen atomically. A relational database gives me ACID transactions, constraints that protect data integrity (unique SKUs, non-null prices, optimistic locking via `@Version`) and a well-defined schema that evolves through versioned Flyway migrations. I don't need the flexible schema or horizontal write scaling that NoSQL is good at, so it would add trade-offs without a real benefit here.

### MapStruct instead of hand-written mappers
Hand-written mappers are fine with a handful of fields, but they're easy to get wrong as entities grow: add a field and forget to map it, and you get a silent `null`. MapStruct generates the mapping code at compile time, so it's plain Java with no reflection overhead, and with `unmappedTargetPolicy = ERROR` a missing mapping breaks the build instead of reaching production. It also keeps mappers short and consistent across modules.

### Modular monolith (Spring Modulith)
Each business capability (`product` for now, later `order`, `customer`, `payment`...) is its own module with a small public API, while its web, service, domain and repository layers stay internal. This gives me clear boundaries and low coupling without the operational cost of microservices: one deployable, one database, simple local development. Modules talk through their public API or domain events, and `ModularityTests` verifies the boundaries on every build, so they don't erode over time. If a module ever needs to scale independently, it can be extracted into a service with little friction.

### Concrete services instead of Interface + Impl
Services are plain `@Service` classes, not a `XService` interface plus a `XServiceImpl`. Spring proxies classes (CGLIB) for `@Transactional` and `@Validated`, Mockito mocks classes just as easily, and each service has exactly one implementation. Services also live in the module's internal `service` package, so an interface there would hide nothing. When another module needs a capability, a public interface (e.g. `product/ProductApi`) is added in the module root and implemented by the internal service. That boundary is where an interface pays off.

### SKUs can be reused after a product is deleted
Products are soft-deleted, so the row stays for history (orders, audits) but the product is no longer active. A SKU identifies an active product in the catalog, so once a product is removed the business should be able to reuse that SKU for a new one. Uniqueness is enforced only among active products: MySQL has no partial indexes, so a virtual column (`active_sku`) holds the SKU for active rows and `NULL` for deleted ones, and the unique index sits on that column. The service also checks for duplicates up front to return a clear `409 Conflict`.
