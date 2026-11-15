package com.example.data

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

enum class EnquiryStatus(val apiValue: String) {
  NEW("new"),
  CONTACTED("contacted"),
  CLOSED("closed");

  companion object {
    fun fromApiValue(raw: String?): EnquiryStatus {
      return when (raw?.trim()?.lowercase()) {
        "contacted" -> CONTACTED
        "closed" -> CLOSED
        else -> NEW
      }
    }
  }
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
  val fullAddress: String = "",
  val landmark: String = "",
  val imageResId: Int = R.drawable.img_benin_house_exterior,
  val imageResList: List<Int> = listOf(R.drawable.img_benin_house_exterior),
  val photoUris: List<String> = emptyList(),
  val facilities: List<String> = emptyList(),
  val additionalFacilitiesNote: String = "",
  val rentalTerms: String = "1 Year rent advance. Physical inspection coordinated through Dwello Homes.",
  val availabilityStatus: String = "Available for Inspection",
  val approvalStatus: PropertyApprovalStatus = PropertyApprovalStatus.PENDING_APPROVAL,
  val rejectionReason: String? = null,
  val submissionTimestamp: Long = System.currentTimeMillis(),
  val isDemonstrationData: Boolean = false
)

data class LandlordDashboardStats(
  val totalProperties: Int,
  val approvedCount: Int,
  val pendingCount: Int,
  val rejectedCount: Int,
  val unavailableCount: Int,
  val newEnquiriesCount: Int
)

fun calculateLandlordStats(
  properties: List<PropertyItem>,
  enquiries: List<EnquiryItem>
): LandlordDashboardStats {
  val total = properties.size
  val approved = properties.count { it.approvalStatus == PropertyApprovalStatus.APPROVED }
  val pending = properties.count { it.approvalStatus == PropertyApprovalStatus.PENDING_APPROVAL }
  val rejected = properties.count { it.approvalStatus == PropertyApprovalStatus.REJECTED }
  val unavailable = properties.count { it.approvalStatus == PropertyApprovalStatus.UNAVAILABLE }
  val newEnquiries = enquiries.count { it.status == EnquiryStatus.NEW }

  return LandlordDashboardStats(
    totalProperties = total,
    approvedCount = approved,
    pendingCount = pending,
    rejectedCount = rejected,
    unavailableCount = unavailable,
    newEnquiriesCount = newEnquiries
  )
}

data class PropertyValidationResult(
  val isValid: Boolean,
  val titleError: String? = null,
  val typeError: String? = null,
  val rentError: String? = null,
  val bedroomsError: String? = null,
  val neighbourhoodError: String? = null,
  val descriptionError: String? = null,
  val photosError: String? = null
)

object PropertyFormValidator {
  fun validate(
    title: String,
    propertyType: String,
    rentAmount: Long,
    bedrooms: Int,
    neighbourhood: String,
    description: String,
    photoCount: Int
  ): PropertyValidationResult {
    val titleErr = if (title.trim().isEmpty()) "Title is required" else null
    val typeErr = if (propertyType.trim().isEmpty()) "Property type is required" else null
    val rentErr = if (rentAmount <= 0L) "Rent amount must be greater than zero" else null
    val bedroomsErr = if (bedrooms < 1 && propertyType != "Commercial Shop") "Bedrooms must be at least 1" else null
    val neighbourhoodErr = if (neighbourhood.trim().isEmpty()) "Neighbourhood is required" else null
    val descErr = if (description.trim().isEmpty()) "Description is required" else null
    val photosErr = if (photoCount < 1) "At least one photo is required" else null

    val valid = titleErr == null &&
      typeErr == null &&
      rentErr == null &&
      bedroomsErr == null &&
      neighbourhoodErr == null &&
      descErr == null &&
      photosErr == null

    return PropertyValidationResult(
      isValid = valid,
      titleError = titleErr,
      typeError = typeErr,
      rentError = rentErr,
      bedroomsError = bedroomsErr,
      neighbourhoodError = neighbourhoodErr,
      descriptionError = descErr,
      photosError = photosErr
    )
  }
}

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
  val tenantPhone: String = "",
  val preferredInspectionDate: String,
  val message: String,
  val createdAt: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val status: EnquiryStatus = EnquiryStatus.NEW,
  val statusEn: String = "New Enquiry",
  val statusPidgin: String = "New Enquiry"
)

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

  val standardFacilitiesList = listOf(
    "Water supply",
    "Electricity",
    "Parking space",
    "Security",
    "Tiled floors",
    "Fitted kitchen",
    "Running water",
    "Fenced compound",
    "Air conditioning",
    "Other facilities"
  )
}
