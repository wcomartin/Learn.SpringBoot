package com.learn.springboot.config

import com.learn.springboot.auth.*
import com.learn.springboot.customer.company.*
import com.learn.springboot.customer.contact.*
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

@Configuration
class DataSeeder {

    @Bean
    fun seed(
        @org.springframework.beans.factory.annotation.Value("\${app.seed:false}") shouldSeed: Boolean,
        userRepo: UserRepository,
        companyRepo: CompanyRepository,
        contactRepo: ContactRepository,
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

        // Users
        userRepo.save(User(email = "admin@example.com", password = passwordEncoder.encode("password"), name = "Admin", role = Role.ADMIN))
        userRepo.save(User(email = "user@example.com", password = passwordEncoder.encode("password"), name = "Staff User", role = Role.USER))

        // Companies
        val acme = companyRepo.save(Company(name = "Acme Corp", email = "info@acme.com", phone = "555-0100", status = CompanyStatus.ACTIVE))
        val globex = companyRepo.save(Company(name = "Globex Inc", email = "hello@globex.com", website = "https://globex.com", status = CompanyStatus.ACTIVE))
        companyRepo.save(Company(name = "Initech", email = "sales@initech.com", status = CompanyStatus.LEAD))

        // Contacts
        val john = contactRepo.save(Contact(firstName = "John", lastName = "Smith", email = "john@acme.com", jobTitle = "VP Purchasing", company = acme))
        contactRepo.save(Contact(firstName = "Jane", lastName = "Doe", email = "jane@acme.com", jobTitle = "Buyer", company = acme))
        contactRepo.save(Contact(firstName = "Bob", lastName = "Wilson", email = "bob@globex.com", jobTitle = "Operations Manager", company = globex))

        acme.primaryContact = john
        companyRepo.save(acme)

        // Warehouses
        val mainWh = warehouseRepo.save(Warehouse(name = "Main Warehouse", address = "100 Industrial Blvd", city = "Toronto", province = "ON", postalCode = "M5V 1A1"))
        val eastWh = warehouseRepo.save(Warehouse(name = "East Distribution", address = "50 Logistics Way", city = "Montreal", province = "QC", postalCode = "H2X 1Y4"))

        // Products
        val widget = productRepo.save(Product(sku = "WDG-001", name = "Standard Widget", unitPrice = BigDecimal("29.99"), costPrice = BigDecimal("12.50"), reorderPoint = 50))
        val premium = productRepo.save(Product(sku = "WDG-002", name = "Premium Widget", unitPrice = BigDecimal("49.99"), costPrice = BigDecimal("22.00"), reorderPoint = 25))
        val gadget = productRepo.save(Product(sku = "GDG-001", name = "Gadget Pro", unitPrice = BigDecimal("99.99"), costPrice = BigDecimal("45.00"), reorderPoint = 10))
        val sprocket = productRepo.save(Product(sku = "SPR-001", name = "Sprocket A", unitPrice = BigDecimal("5.99"), costPrice = BigDecimal("2.10"), reorderPoint = 200))

        // Stock levels
        stockRepo.save(StockLevel(product = widget, warehouse = mainWh, quantity = 120))
        stockRepo.save(StockLevel(product = widget, warehouse = eastWh, quantity = 45))
        stockRepo.save(StockLevel(product = premium, warehouse = mainWh, quantity = 30))
        stockRepo.save(StockLevel(product = gadget, warehouse = mainWh, quantity = 8))  // below reorder point
        stockRepo.save(StockLevel(product = sprocket, warehouse = mainWh, quantity = 150))  // below reorder point
        stockRepo.save(StockLevel(product = sprocket, warehouse = eastWh, quantity = 80))  // below reorder point

        // Suppliers
        val widgetSupply = supplierRepo.save(Supplier(name = "Widget Supply Co", contactName = "Mike Chen", email = "mike@widgetsupply.com", leadTimeDays = 14))
        val partsDirect = supplierRepo.save(Supplier(name = "Parts Direct", contactName = "Sarah Lee", email = "sarah@partsdirect.com", leadTimeDays = 7))

        // Carriers
        val fedex = carrierRepo.save(Carrier(name = "FedEx", trackingUrlTemplate = "https://www.fedex.com/fedextrack/?trknbr={tracking_number}"))
        val ups = carrierRepo.save(Carrier(name = "UPS", trackingUrlTemplate = "https://www.ups.com/track?tracknum={tracking_number}"))
        carrierRepo.save(Carrier(name = "Canada Post", trackingUrlTemplate = "https://www.canadapost-postescanada.ca/track-reperer/en#/search?searchFor={tracking_number}"))

        // --- Sales Orders ---

        // SO-1: Delivered
        val so1 = SalesOrder(orderNumber = "SO-1001", customerId = acme.id, status = SalesOrderStatus.DELIVERED, orderDate = LocalDate.now().minusDays(14))
        so1.lineItems.add(SalesOrderLine(order = so1, productId = widget.id, productSku = widget.sku, productName = widget.name, quantity = 50, unitPrice = widget.unitPrice))
        so1.lineItems.add(SalesOrderLine(order = so1, productId = premium.id, productSku = premium.sku, productName = premium.name, quantity = 10, unitPrice = premium.unitPrice))
        salesOrderRepo.save(so1)

        // SO-2: Shipped
        val so2 = SalesOrder(orderNumber = "SO-1002", customerId = globex.id, status = SalesOrderStatus.SHIPPED, orderDate = LocalDate.now().minusDays(5))
        so2.lineItems.add(SalesOrderLine(order = so2, productId = gadget.id, productSku = gadget.sku, productName = gadget.name, quantity = 5, unitPrice = gadget.unitPrice))
        salesOrderRepo.save(so2)

        // SO-3: Confirmed, awaiting shipment
        val so3 = SalesOrder(orderNumber = "SO-1003", customerId = acme.id, status = SalesOrderStatus.CONFIRMED, orderDate = LocalDate.now().minusDays(2), dueDate = LocalDate.now().plusDays(5))
        so3.lineItems.add(SalesOrderLine(order = so3, productId = widget.id, productSku = widget.sku, productName = widget.name, quantity = 100, unitPrice = widget.unitPrice))
        so3.lineItems.add(SalesOrderLine(order = so3, productId = sprocket.id, productSku = sprocket.sku, productName = sprocket.name, quantity = 500, unitPrice = sprocket.unitPrice))
        salesOrderRepo.save(so3)

        // SO-4: Draft
        val so4 = SalesOrder(orderNumber = "SO-1004", customerId = globex.id, status = SalesOrderStatus.DRAFT, orderDate = LocalDate.now())
        so4.lineItems.add(SalesOrderLine(order = so4, productId = premium.id, productSku = premium.sku, productName = premium.name, quantity = 20, unitPrice = premium.unitPrice))
        salesOrderRepo.save(so4)

        // SO-5: Cancelled
        val so5 = SalesOrder(orderNumber = "SO-1005", customerId = acme.id, status = SalesOrderStatus.CANCELLED, orderDate = LocalDate.now().minusDays(10))
        so5.lineItems.add(SalesOrderLine(order = so5, productId = gadget.id, productSku = gadget.sku, productName = gadget.name, quantity = 2, unitPrice = gadget.unitPrice))
        salesOrderRepo.save(so5)

        // --- Purchase Orders ---

        // PO-1: Received
        val po1 = PurchaseOrder(poNumber = "PO-2001", supplier = widgetSupply, status = PurchaseOrderStatus.RECEIVED, orderDate = LocalDate.now().minusDays(21), expectedDelivery = LocalDate.now().minusDays(7), warehouseId = mainWh.id)
        po1.lineItems.add(PurchaseOrderLine(order = po1, productId = widget.id, productSku = widget.sku, productName = widget.name, quantity = 200, receivedQuantity = 200, unitCost = widget.costPrice))
        purchaseOrderRepo.save(po1)

        // PO-2: Acknowledged, in transit
        val po2 = PurchaseOrder(poNumber = "PO-2002", supplier = partsDirect, status = PurchaseOrderStatus.ACKNOWLEDGED, orderDate = LocalDate.now().minusDays(5), expectedDelivery = LocalDate.now().plusDays(2), warehouseId = mainWh.id)
        po2.lineItems.add(PurchaseOrderLine(order = po2, productId = sprocket.id, productSku = sprocket.sku, productName = sprocket.name, quantity = 500, receivedQuantity = 0, unitCost = sprocket.costPrice))
        po2.lineItems.add(PurchaseOrderLine(order = po2, productId = gadget.id, productSku = gadget.sku, productName = gadget.name, quantity = 20, receivedQuantity = 0, unitCost = gadget.costPrice))
        purchaseOrderRepo.save(po2)

        // PO-3: Submitted
        val po3 = PurchaseOrder(poNumber = "PO-2003", supplier = widgetSupply, status = PurchaseOrderStatus.SUBMITTED, orderDate = LocalDate.now().minusDays(1), expectedDelivery = LocalDate.now().plusDays(13), warehouseId = eastWh.id)
        po3.lineItems.add(PurchaseOrderLine(order = po3, productId = premium.id, productSku = premium.sku, productName = premium.name, quantity = 50, receivedQuantity = 0, unitCost = premium.costPrice))
        purchaseOrderRepo.save(po3)

        // PO-4: Draft
        val po4 = PurchaseOrder(poNumber = "PO-2004", supplier = partsDirect, status = PurchaseOrderStatus.DRAFT, orderDate = LocalDate.now(), warehouseId = mainWh.id)
        po4.lineItems.add(PurchaseOrderLine(order = po4, productId = widget.id, productSku = widget.sku, productName = widget.name, quantity = 100, receivedQuantity = 0, unitCost = widget.costPrice))
        purchaseOrderRepo.save(po4)

        // --- Shipments ---

        // SHP-1: Delivered (for SO-1)
        shipmentRepo.save(Shipment(shipmentNumber = "SHP-3001", salesOrderId = so1.id, warehouseId = mainWh.id, carrier = fedex, trackingNumber = "FX123456789", status = ShipmentStatus.DELIVERED, shippedAt = Instant.now().minusSeconds(86400 * 12), deliveredAt = Instant.now().minusSeconds(86400 * 10)))

        // SHP-2: In transit (for SO-2)
        shipmentRepo.save(Shipment(shipmentNumber = "SHP-3002", salesOrderId = so2.id, warehouseId = mainWh.id, carrier = ups, trackingNumber = "1Z999AA10123456784", status = ShipmentStatus.IN_TRANSIT, shippedAt = Instant.now().minusSeconds(86400 * 3), estimatedDelivery = LocalDate.now().plusDays(2)))

        // SHP-3: Packed, ready to ship (for SO-3)
        shipmentRepo.save(Shipment(shipmentNumber = "SHP-3003", salesOrderId = so3.id, warehouseId = mainWh.id, carrier = fedex, status = ShipmentStatus.PACKED, estimatedDelivery = LocalDate.now().plusDays(4)))

        // SHP-4: Pending (for SO-3, second shipment)
        shipmentRepo.save(Shipment(shipmentNumber = "SHP-3004", salesOrderId = so3.id, warehouseId = eastWh.id, status = ShipmentStatus.PENDING))

        println("✓ Seed data loaded")
    }
}
