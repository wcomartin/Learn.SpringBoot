package com.learn.springboot.config

import com.learn.springboot.auth.*
import com.learn.springboot.customer.company.*
import com.learn.springboot.customer.contact.*
import com.learn.springboot.customer.note.*
import com.learn.springboot.inventory.product.*
import com.learn.springboot.inventory.stock.*
import com.learn.springboot.inventory.warehouse.*
import com.learn.springboot.purchasing.purchaseorder.*
import com.learn.springboot.purchasing.supplier.*
import com.learn.springboot.sales.order.*
import com.learn.springboot.shipping.carrier.*
import com.learn.springboot.shipping.shipment.*
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import kotlin.random.Random

@Configuration
class DataSeeder {

    @Bean
    fun seed(
        @org.springframework.beans.factory.annotation.Value("\${app.seed:false}") shouldSeed: Boolean,
        userRepo: UserRepository,
        companyRepo: CompanyRepository,
        contactRepo: ContactRepository,
        noteRepo: NoteRepository,
        productRepo: ProductRepository,
        warehouseRepo: WarehouseRepository,
        supplierRepo: SupplierRepository,
        carrierRepo: CarrierRepository,
        salesOrderRepo: SalesOrderRepository,
        purchaseOrderRepo: PurchaseOrderRepository,
        shipmentRepo: ShipmentRepository,
        stockRepo: StockLevelRepository,
        passwordEncoder: PasswordEncoder
    ) = CommandLineRunner {
        if (!shouldSeed && userRepo.count() > 0) return@CommandLineRunner

        val rng = Random(42) // deterministic for reproducibility

        // Users
        userRepo.save(User(email = "admin@example.com", password = passwordEncoder.encode("password"), name = "Admin", role = Role.ADMIN))
        userRepo.save(User(email = "user@example.com", password = passwordEncoder.encode("password"), name = "Staff User", role = Role.USER))

        // Companies
        val companyNames = listOf(
            "Acme Corp", "Globex Inc", "Initech", "Umbrella Industries", "Stark Supplies",
            "Wayne Enterprises", "Cyberdyne Systems", "Soylent Corp", "Massive Dynamic",
            "Oscorp Industries", "LexCorp", "Aperture Science", "Black Mesa Research",
            "Tyrell Corporation", "Weyland-Yutani", "Abstergo Industries", "Vault-Tec",
            "Mann Co", "Hyperion Corp", "Atlas Solutions"
        )
        val companies = companyNames.mapIndexed { i, name ->
            companyRepo.save(Company(
                name = name,
                email = "${name.lowercase().replace(" ", "").take(10)}@example.com",
                phone = "555-${String.format("%04d", i * 111)}",
                website = if (rng.nextBoolean()) "https://${name.lowercase().replace(" ", "")}.com" else null,
                status = listOf(CompanyStatus.ACTIVE, CompanyStatus.ACTIVE, CompanyStatus.ACTIVE, CompanyStatus.LEAD, CompanyStatus.INACTIVE)[rng.nextInt(5)]
            ))
        }

        // Contacts
        val firstNames = listOf("John", "Jane", "Bob", "Alice", "Mike", "Sarah", "Tom", "Lisa", "Dave", "Emma", "Chris", "Rachel", "James", "Maria", "Kevin", "Laura", "Steve", "Nicole", "Brian", "Amy", "Dan", "Kate", "Paul", "Megan", "Eric", "Julie", "Mark", "Anna", "Scott", "Diana")
        val lastNames = listOf("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Wilson", "Anderson", "Taylor", "Thomas", "Moore", "Jackson", "Martin", "Lee", "Thompson", "White")
        val titles = listOf("VP Purchasing", "Buyer", "Operations Manager", "CEO", "CFO", "Warehouse Manager", "Procurement Lead", "Supply Chain Director", "Logistics Coordinator", "Account Manager")

        val contacts = (0 until 50).map { i ->
            val company = companies[rng.nextInt(companies.size)]
            contactRepo.save(Contact(
                firstName = firstNames[rng.nextInt(firstNames.size)],
                lastName = lastNames[rng.nextInt(lastNames.size)],
                email = "${firstNames[i % firstNames.size].lowercase()}.${lastNames[i % lastNames.size].lowercase()}@example.com",
                phone = "555-${String.format("%04d", rng.nextInt(10000))}",
                jobTitle = titles[rng.nextInt(titles.size)],
                company = company
            ))
        }

        // Set primary contacts
        companies.forEach { company ->
            val companyContacts = contacts.filter { it.company?.id == company.id }
            if (companyContacts.isNotEmpty()) {
                company.primaryContact = companyContacts.first()
                companyRepo.save(company)
            }
        }

        // Notes
        val noteKinds = NoteKind.entries
        val noteBodies = listOf(
            "Discussed pricing for Q2 order", "Follow up on delivery schedule",
            "Sent updated catalog", "Confirmed payment terms", "Reviewed contract renewal",
            "Left voicemail about outstanding invoice", "Met at trade show, interested in bulk order",
            "Requested samples of new product line", "Negotiated 10% volume discount",
            "Scheduled site visit for next week", "Resolved shipping complaint",
            "Introduced to new procurement team member", "Discussed return policy",
            "Sent quote for 500 units", "Called about delayed shipment"
        )
        contacts.take(20).forEach { contact ->
            (0 until rng.nextInt(1, 5)).forEach { _ ->
                noteRepo.save(Note(
                    body = noteBodies[rng.nextInt(noteBodies.size)],
                    kind = noteKinds[rng.nextInt(noteKinds.size)],
                    contact = contact
                ))
            }
        }

        // Warehouses
        val warehouses = listOf(
            warehouseRepo.save(Warehouse(name = "Main Warehouse", address = "100 Industrial Blvd", city = "Toronto", province = "ON", postalCode = "M5V 1A1")),
            warehouseRepo.save(Warehouse(name = "East Distribution", address = "50 Logistics Way", city = "Montreal", province = "QC", postalCode = "H2X 1Y4")),
            warehouseRepo.save(Warehouse(name = "West Fulfillment", address = "200 Pacific Dr", city = "Vancouver", province = "BC", postalCode = "V6B 2P1"))
        )

        // Products
        val productData = listOf(
            Triple("WDG-001", "Standard Widget", Pair("29.99", "12.50")),
            Triple("WDG-002", "Premium Widget", Pair("49.99", "22.00")),
            Triple("WDG-003", "Mini Widget", Pair("14.99", "6.25")),
            Triple("GDG-001", "Gadget Pro", Pair("99.99", "45.00")),
            Triple("GDG-002", "Gadget Lite", Pair("59.99", "28.00")),
            Triple("GDG-003", "Gadget Max", Pair("149.99", "67.50")),
            Triple("SPR-001", "Sprocket A", Pair("5.99", "2.10")),
            Triple("SPR-002", "Sprocket B", Pair("7.99", "3.20")),
            Triple("SPR-003", "Sprocket C (Heavy Duty)", Pair("12.99", "5.50")),
            Triple("BLT-001", "Bolt Pack (100)", Pair("19.99", "8.00")),
            Triple("BLT-002", "Bolt Pack (500)", Pair("79.99", "32.00")),
            Triple("GKT-001", "Gasket Set", Pair("34.99", "15.00")),
            Triple("GKT-002", "Premium Gasket Set", Pair("54.99", "24.00")),
            Triple("BRG-001", "Ball Bearing 10mm", Pair("8.99", "3.50")),
            Triple("BRG-002", "Ball Bearing 20mm", Pair("12.99", "5.00")),
            Triple("MTR-001", "Servo Motor Small", Pair("89.99", "40.00")),
            Triple("MTR-002", "Servo Motor Large", Pair("199.99", "90.00")),
            Triple("SEN-001", "Temperature Sensor", Pair("24.99", "10.00")),
            Triple("SEN-002", "Pressure Sensor", Pair("44.99", "18.00")),
            Triple("CBL-001", "Cable Assembly 1m", Pair("15.99", "6.50"))
        )
        val products = productData.map { (sku, name, prices) ->
            productRepo.save(Product(
                sku = sku, name = name,
                unitPrice = BigDecimal(prices.first), costPrice = BigDecimal(prices.second),
                reorderPoint = rng.nextInt(10, 100),
                status = if (rng.nextInt(10) < 8) ProductStatus.ACTIVE else ProductStatus.DISCONTINUED
            ))
        }

        // Stock levels
        products.filter { it.status == ProductStatus.ACTIVE }.forEach { product ->
            warehouses.forEach { warehouse ->
                if (rng.nextBoolean() || warehouse == warehouses[0]) {
                    stockRepo.save(StockLevel(
                        product = product, warehouse = warehouse,
                        quantity = rng.nextInt(0, 300)
                    ))
                }
            }
        }

        // Suppliers
        val suppliers = listOf(
            supplierRepo.save(Supplier(name = "Widget Supply Co", contactName = "Mike Chen", email = "mike@widgetsupply.com", leadTimeDays = 14)),
            supplierRepo.save(Supplier(name = "Parts Direct", contactName = "Sarah Lee", email = "sarah@partsdirect.com", leadTimeDays = 7)),
            supplierRepo.save(Supplier(name = "Industrial Components Ltd", contactName = "Tom Park", email = "tom@industrialcomp.com", leadTimeDays = 21)),
            supplierRepo.save(Supplier(name = "FastParts Express", contactName = "Lisa Wong", email = "lisa@fastparts.com", leadTimeDays = 3)),
            supplierRepo.save(Supplier(name = "Global Manufacturing", contactName = "James Rivera", email = "james@globalmfg.com", leadTimeDays = 30))
        )

        // Carriers
        val carriers = listOf(
            carrierRepo.save(Carrier(name = "FedEx", trackingUrlTemplate = "https://www.fedex.com/fedextrack/?trknbr={tracking_number}")),
            carrierRepo.save(Carrier(name = "UPS", trackingUrlTemplate = "https://www.ups.com/track?tracknum={tracking_number}")),
            carrierRepo.save(Carrier(name = "Canada Post", trackingUrlTemplate = "https://www.canadapost-postescanada.ca/track-reperer/en#/search?searchFor={tracking_number}")),
            carrierRepo.save(Carrier(name = "DHL", trackingUrlTemplate = "https://www.dhl.com/en/express/tracking.html?AWB={tracking_number}"))
        )

        // Sales Orders (25 orders in various states)
        val soStatuses = SalesOrderStatus.entries
        val salesOrders = (1..25).map { i ->
            val status = soStatuses[rng.nextInt(soStatuses.size)]
            val daysAgo = rng.nextInt(1, 60)
            val order = SalesOrder(
                orderNumber = "SO-${1000 + i}",
                customerId = companies[rng.nextInt(companies.size)].id,
                status = status,
                orderDate = LocalDate.now().minusDays(daysAgo.toLong()),
                dueDate = if (rng.nextBoolean()) LocalDate.now().plusDays(rng.nextInt(1, 14).toLong()) else null
            )
            val lineCount = rng.nextInt(1, 5)
            val usedProducts = products.shuffled(rng).take(lineCount)
            usedProducts.forEach { p ->
                order.lineItems.add(SalesOrderLine(
                    order = order, productId = p.id, productSku = p.sku, productName = p.name,
                    quantity = rng.nextInt(1, 100), unitPrice = p.unitPrice
                ))
            }
            salesOrderRepo.save(order)
        }

        // Purchase Orders (15 orders)
        val poStatuses = PurchaseOrderStatus.entries
        (1..15).forEach { i ->
            val status = poStatuses[rng.nextInt(poStatuses.size)]
            val daysAgo = rng.nextInt(1, 45)
            val po = PurchaseOrder(
                poNumber = "PO-${2000 + i}",
                supplier = suppliers[rng.nextInt(suppliers.size)],
                status = status,
                orderDate = LocalDate.now().minusDays(daysAgo.toLong()),
                expectedDelivery = LocalDate.now().plusDays(rng.nextInt(-5, 20).toLong()),
                warehouseId = warehouses[rng.nextInt(warehouses.size)].id
            )
            val lineCount = rng.nextInt(1, 4)
            products.shuffled(rng).take(lineCount).forEach { p ->
                val qty = rng.nextInt(10, 200)
                po.lineItems.add(PurchaseOrderLine(
                    order = po, productId = p.id, productSku = p.sku, productName = p.name,
                    quantity = qty,
                    receivedQuantity = if (status == PurchaseOrderStatus.RECEIVED) qty else if (status == PurchaseOrderStatus.ACKNOWLEDGED) rng.nextInt(0, qty) else 0,
                    unitCost = p.costPrice
                ))
            }
            purchaseOrderRepo.save(po)
        }

        // Shipments (20 shipments)
        val shStatuses = ShipmentStatus.entries
        val shippableOrders = salesOrders.filter { it.status != SalesOrderStatus.DRAFT && it.status != SalesOrderStatus.CANCELLED }
        (1..20).map { i ->
            val status = shStatuses[rng.nextInt(shStatuses.size)]
            val carrier = if (rng.nextInt(10) < 8) carriers[rng.nextInt(carriers.size)] else null
            val trackingNumber = if (carrier != null && status != ShipmentStatus.PENDING) "TRK${rng.nextLong(1000000000, 9999999999)}" else null
            shipmentRepo.save(Shipment(
                shipmentNumber = "SHP-${3000 + i}",
                salesOrderId = if (shippableOrders.isNotEmpty()) shippableOrders[rng.nextInt(shippableOrders.size)].id else salesOrders[0].id,
                warehouseId = warehouses[rng.nextInt(warehouses.size)].id,
                carrier = carrier,
                trackingNumber = trackingNumber,
                status = status,
                shippedAt = if (status.ordinal >= ShipmentStatus.SHIPPED.ordinal) Instant.now().minusSeconds(rng.nextLong(86400, 864000)) else null,
                deliveredAt = if (status == ShipmentStatus.DELIVERED) Instant.now().minusSeconds(rng.nextLong(3600, 172800)) else null,
                estimatedDelivery = if (status != ShipmentStatus.DELIVERED) LocalDate.now().plusDays(rng.nextInt(1, 10).toLong()) else null
            ))
        }

        println("✓ Seed data loaded: ${companies.size} companies, ${contacts.size} contacts, ${products.size} products, 25 sales orders, 15 POs, 20 shipments")
    }
}
