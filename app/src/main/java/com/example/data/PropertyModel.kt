package com.example.data

import androidx.compose.runtime.mutableStateListOf
import com.example.R

enum class PropertyApprovalStatus {
  PENDING_APPROVAL,
  APPROVED,
  REJECTED,
  UNAVAILABLE
}

enum class RentalPeriod {
  YEARLY,
  MONTHLY
}

data class PropertyItem(
  val id: String,
  val titleEn: String,
  val titlePidgin: String,
  val categoryEn: String,
  val categoryPidgin: String,
  val area: String,
  val priceNaira: Long,
  val priceFormatted: String,
  val rentalPeriod: RentalPeriod = RentalPeriod.YEARLY,
  val bedrooms: Int,
  val bathrooms: Int,
  val isGated: Boolean = true,
  val hasBorehole: Boolean = true,
  val hasPrepaidMeter: Boolean = true,
  val landlordName: String,
  val landlordRoleEn: String = "Property Owner",
  val landlordRolePidgin: String = "House Owner",
  val phoneContact: String = "0800 393 556",
  val descriptionEn: String,
  val descriptionPidgin: String,
  val neighborhoodNote: String,
  val imageResId: Int = R.drawable.img_benin_house_exterior,
  val imageResList: List<Int> = listOf(
    R.drawable.img_benin_house_exterior,
    R.drawable.img_benin_house_interior,
    R.drawable.img_benin_house_modern_1794407034302,
    R.drawable.img_benin_flat_interior_1794407048683
  ),
  val facilities: List<String> = listOf("Borehole Water", "Prepaid Meter", "Fenced & Gated Compound", "POP Ceiling"),
  val rentalTerms: String = "1 Year rent in advance. 10% Caution deposit refundable at vacancy. Tenancy agreement handled via Dwello Homes.",
  val availabilityStatus: String = "Available for Inspection",
  val approvalStatus: PropertyApprovalStatus = PropertyApprovalStatus.APPROVED,
  val isDemonstrationData: Boolean = true
)

data class BeninCityArea(
  val name: String,
  val descriptionEn: String,
  val descriptionPidgin: String,
  val isPopular: Boolean = true
)

data class EnquiryItem(
  val id: String,
  val propertyId: String,
  val propertyTitle: String,
  val propertyArea: String,
  val propertyPrice: String,
  val tenantName: String,
  val tenantPhone: String,
  val preferredInspectionDate: String,
  val message: String,
  val timestamp: Long = System.currentTimeMillis(),
  val statusEn: String = "Pending Inspection Coordination",
  val statusPidgin: String = "Inspection Arrangement Dey Go On"
)

object EnquiryRepository {
  val submittedEnquiries = mutableStateListOf<EnquiryItem>(
    EnquiryItem(
      id = "enq_demo_1",
      propertyId = "prop_1",
      propertyTitle = "Standard Self-Contained Apartment",
      propertyArea = "Ugbowo",
      propertyPrice = "₦250,000 / year",
      tenantName = "Osayi Osazuwa",
      tenantPhone = "0803 123 4567",
      preferredInspectionDate = "Saturday 11:00 AM",
      message = "I want to inspect this flat near UNIBEN main gate. Please coordinate with the owner.",
      statusEn = "Dwello Coordinator Assigned",
      statusPidgin = "Dwello Coordinator Don Ready"
    )
  )

  fun addEnquiry(enquiry: EnquiryItem) {
    submittedEnquiries.add(0, enquiry)
  }
}

object SamplePropertyData {
  val beninAreas = listOf(
    BeninCityArea("GRA", "Quiet, upscale residential with good roads", "Quiet and clean area, good roads"),
    BeninCityArea("Sapele Road", "Serene living along bypass and Country Home", "Cool area with fine compounds"),
    BeninCityArea("Airport Road", "Prime central location, reliable power", "Main road axis, good light"),
    BeninCityArea("Ugbowo", "Close to UNIBEN, UBTH and Uselu Market", "Near UNIBEN main gate and UBTH"),
    BeninCityArea("Ekosodin", "Vibrant student community beside UNIBEN", "Student area right beside school"),
    BeninCityArea("Aduwawa", "Fast developing residential area", "Developing area with modern houses"),
    BeninCityArea("Ikpoba Hill", "High elevation, scenic views, commercial", "Up hill area with busy markets"),
    BeninCityArea("New Benin", "Commercial and central residential hub", "Central area near market"),
    BeninCityArea("Ekenwan", "Affordable student and family living", "Good area for students and family"),
    BeninCityArea("Ogida", "Accessible residential quarter near Siluko", "Accessible area near Siluko axis"),
    BeninCityArea("Other Locations", "Other serene locations across Benin City", "Other places inside Benin")
  )

  val propertyTypesList = listOf(
    "Self-contained",
    "Mini flat",
    "1-bedroom apartment",
    "2-bedroom apartment",
    "3-bedroom apartment",
    "Duplex",
    "Detached house",
    "Shared accommodation"
  )

  // Master property repository
  val dynamicProperties = mutableStateListOf<PropertyItem>(
    // 1. Self-contained in Ugbowo
    PropertyItem(
      id = "prop_1",
      titleEn = "Standard Self-Contained Apartment",
      titlePidgin = "Sharp Self-Con Apartment",
      categoryEn = "Self-contained",
      categoryPidgin = "Self-contained",
      area = "Ugbowo",
      priceNaira = 250000,
      priceFormatted = "₦250,000",
      rentalPeriod = RentalPeriod.YEARLY,
      bedrooms = 1,
      bathrooms = 1,
      isGated = true,
      hasBorehole = true,
      hasPrepaidMeter = true,
      landlordName = "Mr. Osasere",
      landlordRoleEn = "Landlord (Direct)",
      landlordRolePidgin = "Direct House Owner",
      phoneContact = "0800 393 556",
      descriptionEn = "Tiled spacious self-contained room with dedicated kitchen, modern en-suite bathroom, running borehole water, and prepaid electricity meter. Fully gated compound with 24-hour security lock. Very close to UNIBEN main gate and UBTH.",
      descriptionPidgin = "Tiled room with personal kitchen, running tap water and personal prepaid meter. Fenced compound with strong gate. E dey very close to UNIBEN school gate.",
      neighborhoodNote = "Near Uselu Market / UNIBEN Main Gate",
      imageResId = R.drawable.img_benin_house_exterior,
      imageResList = listOf(
        R.drawable.img_benin_house_exterior,
        R.drawable.img_benin_flat_interior_1794407048683,
        R.drawable.img_benin_house_interior
      ),
      facilities = listOf("Borehole Water", "Personal Prepaid Meter", "Gated Compound", "Tiled Floors", "Kitchen Cabinets"),
      rentalTerms = "1 Year rent in advance. ₦25,000 caution deposit. Agreement facilitated by Dwello Homes.",
      availabilityStatus = "Available for Inspection",
      approvalStatus = PropertyApprovalStatus.APPROVED,
      isDemonstrationData = true
    ),

    // 2. 2-Bedroom Flat in GRA
    PropertyItem(
      id = "prop_2",
      titleEn = "Spacious 2-Bedroom Flat",
      titlePidgin = "Big 2-Bedroom Flat",
      categoryEn = "2-bedroom apartment",
      categoryPidgin = "2-bedroom apartment",
      area = "GRA",
      priceNaira = 650000,
      priceFormatted = "₦650,000",
      rentalPeriod = RentalPeriod.YEARLY,
      bedrooms = 2,
      bathrooms = 2,
      isGated = true,
      hasBorehole = true,
      hasPrepaidMeter = true,
      landlordName = "Chief Ighodaro",
      landlordRoleEn = "Property Owner",
      landlordRolePidgin = "House Owner",
      phoneContact = "0800 393 556",
      descriptionEn = "Newly painted 2-bedroom executive flat with POP ceiling, modern wardrobes in both bedrooms, water heater, and ample parking space inside a serene, secure GRA compound with paved interlocked grounds.",
      descriptionPidgin = "New painted 2-bedroom with fine POP ceiling, big wardrobe, water heater and enough space to pack motor inside secure GRA compound.",
      neighborhoodNote = "Off Boundary Road, GRA",
      imageResId = R.drawable.img_benin_house_interior,
      imageResList = listOf(
        R.drawable.img_benin_house_interior,
        R.drawable.img_benin_house_modern_1794407034302,
        R.drawable.img_benin_house_exterior
      ),
      facilities = listOf("Constant Water", "Prepaid Meter", "POP Ceilings", "Interlocked Compound", "Water Heater", "Car Park"),
      rentalTerms = "1 Year advance payment. ₦50,000 caution deposit. Physical inspection coordinated by Dwello Homes.",
      availabilityStatus = "Available for Inspection",
      approvalStatus = PropertyApprovalStatus.APPROVED,
      isDemonstrationData = true
    ),

    // 3. 3-Bedroom Flat in Airport Road
    PropertyItem(
      id = "prop_3",
      titleEn = "Executive 3-Bedroom Apartment",
      titlePidgin = "Fine 3-Bedroom Flat",
      categoryEn = "3-bedroom apartment",
      categoryPidgin = "3-bedroom apartment",
      area = "Airport Road",
      priceNaira = 850000,
      priceFormatted = "₦850,000",
      rentalPeriod = RentalPeriod.YEARLY,
      bedrooms = 3,
      bathrooms = 3,
      isGated = true,
      hasBorehole = true,
      hasPrepaidMeter = true,
      landlordName = "Madam Ngozi",
      landlordRoleEn = "Landlady",
      landlordRolePidgin = "House Owner",
      phoneContact = "0800 393 556",
      descriptionEn = "All rooms en-suite, spacious living room with dining alcove, modern kitchen cabinets, 24/7 borehole water, dedicated transformer, and quiet residential neighborhood on Airport Road axis.",
      descriptionPidgin = "All rooms get personal toilet and bath, big parlor with dining, fine kitchen cabinets, steady borehole water, quiet street with regular light.",
      neighborhoodNote = "Off Oko Central, Airport Road",
      imageResId = R.drawable.img_benin_house_modern_1794407034302,
      imageResList = listOf(
        R.drawable.img_benin_house_modern_1794407034302,
        R.drawable.img_benin_house_interior,
        R.drawable.img_benin_flat_interior_1794407048683
      ),
      facilities = listOf("All En-suite", "Borehole Water", "Prepaid Meter", "Dedicated Transformer", "Security Gate"),
      rentalTerms = "1 Year upfront. Inspection strictly through Dwello Homes accredited officers.",
      availabilityStatus = "Available for Inspection",
      approvalStatus = PropertyApprovalStatus.APPROVED,
      isDemonstrationData = true
    ),

    // 4. Mini flat in Sapele Road
    PropertyItem(
      id = "prop_4",
      titleEn = "Cozy Modern Mini Flat",
      titlePidgin = "Correct Mini Flat (Room & Parlour)",
      categoryEn = "Mini flat",
      categoryPidgin = "Mini flat",
      area = "Sapele Road",
      priceNaira = 380000,
      priceFormatted = "₦380,000",
      rentalPeriod = RentalPeriod.YEARLY,
      bedrooms = 1,
      bathrooms = 1,
      isGated = true,
      hasBorehole = true,
      hasPrepaidMeter = true,
      landlordName = "Elder Omoruyi",
      landlordRoleEn = "Property Owner",
      landlordRolePidgin = "House Owner",
      phoneContact = "0800 393 556",
      descriptionEn = "Charming room and parlor mini flat with private kitchen and balcony. Very clean compound, peaceful neighborhood off Sapele Road bypass, steady borehole water supply.",
      descriptionPidgin = "Room and parlor mini flat with personal kitchen and balcony. Fine compound, quiet area for Sapele Road with good water.",
      neighborhoodNote = "Near Country Home Motel axis",
      imageResId = R.drawable.img_benin_flat_interior_1794407048683,
      imageResList = listOf(
        R.drawable.img_benin_flat_interior_1794407048683,
        R.drawable.img_benin_house_exterior
      ),
      facilities = listOf("Running Borehole", "Personal Meter", "Balcony", "Gated Compound"),
      rentalTerms = "1 Year payment. Inspection booked via Dwello Homes.",
      availabilityStatus = "Available for Inspection",
      approvalStatus = PropertyApprovalStatus.APPROVED,
      isDemonstrationData = true
    ),

    // 5. 1-Bedroom Apartment in Aduwawa
    PropertyItem(
      id = "prop_5",
      titleEn = "Contemporary 1-Bedroom Apartment",
      titlePidgin = "Sharp 1-Bedroom Flat",
      categoryEn = "1-bedroom apartment",
      categoryPidgin = "1-bedroom apartment",
      area = "Aduwawa",
      priceNaira = 320000,
      priceFormatted = "₦320,000",
      rentalPeriod = RentalPeriod.YEARLY,
      bedrooms = 1,
      bathrooms = 1,
      isGated = true,
      hasBorehole = true,
      hasPrepaidMeter = true,
      landlordName = "Mr. Lucky",
      landlordRoleEn = "Landlord",
      landlordRolePidgin = "Landlord",
      phoneContact = "0800 393 556",
      descriptionEn = "Neatly built 1-bedroom flat with cross ventilation, personal prepaid meter, modern sanitary fittings, and secure fencing with gated entrance in Aduwawa.",
      descriptionPidgin = "Sharp 1-bedroom flat with personal meter, fine tiles and strong security gate inside developing Aduwawa area.",
      neighborhoodNote = "Aduwawa residential layout",
      imageResId = R.drawable.img_benin_house_exterior,
      imageResList = listOf(
        R.drawable.img_benin_house_exterior,
        R.drawable.img_benin_house_interior
      ),
      facilities = listOf("Water Supply", "Prepaid Meter", "Fence & Gate", "Tiled Flooring"),
      rentalTerms = "1 Year rental term.",
      availabilityStatus = "Available for Inspection",
      approvalStatus = PropertyApprovalStatus.APPROVED,
      isDemonstrationData = true
    ),

    // 6. Duplex in GRA (High-end)
    PropertyItem(
      id = "prop_6",
      titleEn = "Luxury 4-Bedroom Semi-Detached Duplex",
      titlePidgin = "Big 4-Bedroom Duplex",
      categoryEn = "Duplex",
      categoryPidgin = "Duplex",
      area = "GRA",
      priceNaira = 2200000,
      priceFormatted = "₦2,200,000",
      rentalPeriod = RentalPeriod.YEARLY,
      bedrooms = 4,
      bathrooms = 4,
      isGated = true,
      hasBorehole = true,
      hasPrepaidMeter = true,
      landlordName = "Barrister Imasuen",
      landlordRoleEn = "Property Owner",
      landlordRolePidgin = "House Owner",
      phoneContact = "0800 393 556",
      descriptionEn = "Magnificent duplex with fitted kitchen, servant quarters (BQ), automated gate, stamped concrete compound, perimeter electric fencing, and serene VIP security neighborhood in GRA.",
      descriptionPidgin = "Fine duplex with ready kitchen, BQ, automatic gate, electric wire on fence and heavy security inside GRA.",
      neighborhoodNote = "Golf Course axis, GRA",
      imageResId = R.drawable.img_benin_house_modern_1794407034302,
      imageResList = listOf(
        R.drawable.img_benin_house_modern_1794407034302,
        R.drawable.img_benin_house_interior,
        R.drawable.img_benin_flat_interior_1794407048683
      ),
      facilities = listOf("Electric Fence", "Fitted Kitchen", "BQ Included", "Borehole & Treatment Plant", "Ample Car Park"),
      rentalTerms = "1 or 2 Years advance. Strictly verified tenants.",
      availabilityStatus = "Available for Inspection",
      approvalStatus = PropertyApprovalStatus.APPROVED,
      isDemonstrationData = true
    ),

    // 7. Student Self-contained in Ekosodin
    PropertyItem(
      id = "prop_7",
      titleEn = "Budget Student Self-Contained",
      titlePidgin = "Student Self-Con for Ekosodin",
      categoryEn = "Self-contained",
      categoryPidgin = "Self-contained",
      area = "Ekosodin",
      priceNaira = 180000,
      priceFormatted = "₦180,000",
      rentalPeriod = RentalPeriod.YEARLY,
      bedrooms = 1,
      bathrooms = 1,
      isGated = true,
      hasBorehole = true,
      hasPrepaidMeter = true,
      landlordName = "Pastor Edebiri",
      landlordRoleEn = "Landlord",
      landlordRolePidgin = "Landlord",
      phoneContact = "0800 393 556",
      descriptionEn = "Tiled room with personal kitchen counter and bathroom. Water runs reliably from compound borehole. 5 minutes walk into UNIBEN via Ekosodin small gate.",
      descriptionPidgin = "Clean student room with personal toilet and kitchen. Water dey run and e dey 5 minutes waka enter school gate.",
      neighborhoodNote = "Near UNIBEN Ekosodin Gate",
      imageResId = R.drawable.img_benin_flat_interior_1794407048683,
      imageResList = listOf(
        R.drawable.img_benin_flat_interior_1794407048683,
        R.drawable.img_benin_house_exterior
      ),
      facilities = listOf("Compound Borehole", "Prepaid Meter", "Near Campus", "Fenced"),
      rentalTerms = "1 Year student tenancy term.",
      availabilityStatus = "Available for Inspection",
      approvalStatus = PropertyApprovalStatus.APPROVED,
      isDemonstrationData = true
    ),

    // 8. Shared Accommodation in Ekenwan
    PropertyItem(
      id = "prop_8",
      titleEn = "Affordable Shared Accommodation",
      titlePidgin = "Decent Shared Room",
      categoryEn = "Shared accommodation",
      categoryPidgin = "Shared accommodation",
      area = "Ekenwan",
      priceNaira = 85000,
      priceFormatted = "₦85,000",
      rentalPeriod = RentalPeriod.YEARLY,
      bedrooms = 1,
      bathrooms = 1,
      isGated = true,
      hasBorehole = true,
      hasPrepaidMeter = false,
      landlordName = "Baba Monday",
      landlordRoleEn = "Landlord",
      landlordRolePidgin = "Landlord",
      phoneContact = "0800 393 556",
      descriptionEn = "Cemented single room with clean shared facilities. Fenced compound with well-maintained borehole water and calm living environment near Ekenwan Barracks.",
      descriptionPidgin = "Clean room, fenced compound with gate, clean shared tap water and toilet. Cheap rent and calm environment.",
      neighborhoodNote = "Near Ekenwan Barracks",
      imageResId = R.drawable.img_benin_house_interior,
      imageResList = listOf(
        R.drawable.img_benin_house_interior,
        R.drawable.img_benin_house_exterior
      ),
      facilities = listOf("Compound Water", "Fenced Compound", "Low Rent"),
      rentalTerms = "1 Year rent.",
      availabilityStatus = "Available for Inspection",
      approvalStatus = PropertyApprovalStatus.APPROVED,
      isDemonstrationData = true
    ),

    // 9. Monthly rental in Ikpoba Hill
    PropertyItem(
      id = "prop_9",
      titleEn = "Furnished 1-Bedroom (Monthly Option)",
      titlePidgin = "Furnished 1-Bed (Monthly Rent)",
      categoryEn = "1-bedroom apartment",
      categoryPidgin = "1-bedroom apartment",
      area = "Ikpoba Hill",
      priceNaira = 45000,
      priceFormatted = "₦45,000",
      rentalPeriod = RentalPeriod.MONTHLY,
      bedrooms = 1,
      bathrooms = 1,
      isGated = true,
      hasBorehole = true,
      hasPrepaidMeter = true,
      landlordName = "Mr. Ehis",
      landlordRoleEn = "Property Owner",
      landlordRolePidgin = "House Owner",
      phoneContact = "0800 393 556",
      descriptionEn = "Short or long stay monthly flexible apartment with mattress, fan, private bath and kitchen. Clean compound on high ground in Ikpoba Hill.",
      descriptionPidgin = "Monthly rent apartment with bed and personal toilet. Very neat compound on top Ikpoba Hill.",
      neighborhoodNote = "Ikpoba Hill Express axis",
      imageResId = R.drawable.img_benin_house_exterior,
      imageResList = listOf(
        R.drawable.img_benin_house_exterior,
        R.drawable.img_benin_house_interior
      ),
      facilities = listOf("Monthly Payment", "Borehole Water", "Prepaid Meter", "High Ground"),
      rentalTerms = "Monthly rent payable at the start of each month.",
      availabilityStatus = "Available for Inspection",
      approvalStatus = PropertyApprovalStatus.APPROVED,
      isDemonstrationData = true
    ),

    // 10. Detached House in Ogida
    PropertyItem(
      id = "prop_10",
      titleEn = "Gated 3-Bedroom Detached House",
      titlePidgin = "Personal 3-Bedroom Compound",
      categoryEn = "Detached house",
      categoryPidgin = "Detached house",
      area = "Ogida",
      priceNaira = 750000,
      priceFormatted = "₦750,000",
      rentalPeriod = RentalPeriod.YEARLY,
      bedrooms = 3,
      bathrooms = 2,
      isGated = true,
      hasBorehole = true,
      hasPrepaidMeter = true,
      landlordName = "Chief Obaseki",
      landlordRoleEn = "Property Owner",
      landlordRolePidgin = "House Owner",
      phoneContact = "0800 393 556",
      descriptionEn = "Standalone detached bungalow inside personal private compound. Ample parking for up to 3 cars, private borehole, fully paved yard in Ogida quarter.",
      descriptionPidgin = "Bungalow wey stand on its own with big private compound. You fit park 3 motors and water dey.",
      neighborhoodNote = "Near Ogida police barrack axis",
      imageResId = R.drawable.img_benin_house_modern_1794407034302,
      imageResList = listOf(
        R.drawable.img_benin_house_modern_1794407034302,
        R.drawable.img_benin_house_exterior
      ),
      facilities = listOf("Private Compound", "Dedicated Borehole", "Car Parking for 3 Cars", "Gated Security"),
      rentalTerms = "1 Year advance.",
      availabilityStatus = "Available for Inspection",
      approvalStatus = PropertyApprovalStatus.APPROVED,
      isDemonstrationData = true
    )
  )

  /**
   * Rule: Newly submitted properties must automatically receive Pending Approval status.
   * Pending properties must never appear in public tenant search results.
   */
  fun addProperty(item: PropertyItem) {
    // Enforce business rule: new landlord properties are always PENDING_APPROVAL
    val pendingItem = item.copy(approvalStatus = PropertyApprovalStatus.PENDING_APPROVAL)
    dynamicProperties.add(0, pendingItem)
  }

  /**
   * Public search query: ONLY APPROVED properties appear to tenants!
   */
  fun getApprovedPublicProperties(): List<PropertyItem> {
    return dynamicProperties.filter { it.approvalStatus == PropertyApprovalStatus.APPROVED }
  }

  /**
   * Admin verification action to approve/reject property
   */
  fun updatePropertyStatus(propertyId: String, newStatus: PropertyApprovalStatus) {
    val index = dynamicProperties.indexOfFirst { it.id == propertyId }
    if (index != -1) {
      val current = dynamicProperties[index]
      dynamicProperties[index] = current.copy(approvalStatus = newStatus)
    }
  }
}
