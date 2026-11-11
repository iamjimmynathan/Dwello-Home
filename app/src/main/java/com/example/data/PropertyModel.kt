package com.example.data

import androidx.compose.runtime.mutableStateListOf
import com.example.R

data class PropertyItem(
  val id: String,
  val titleEn: String,
  val titlePidgin: String,
  val categoryEn: String,
  val categoryPidgin: String,
  val area: String,
  val priceNaira: Long,
  val priceFormatted: String,
  val bedrooms: Int,
  val bathrooms: Int,
  val isGated: Boolean,
  val hasBorehole: Boolean,
  val hasPrepaidMeter: Boolean,
  val landlordName: String,
  val landlordRoleEn: String,
  val landlordRolePidgin: String,
  val phoneContact: String,
  val descriptionEn: String,
  val descriptionPidgin: String,
  val neighborhoodNote: String,
  val imageResId: Int = R.drawable.img_benin_house_exterior,
  val isDemonstrationData: Boolean = true
)

data class BeninCityArea(
  val name: String,
  val descriptionEn: String,
  val descriptionPidgin: String,
  val isPopular: Boolean = true
)

object SamplePropertyData {
  val beninAreas = listOf(
    BeninCityArea("GRA", "Quiet, upscale residential with good roads", "Quiet and clean area, good roads"),
    BeninCityArea("Uselu / Ugbowo", "Close to UNIBEN and UBTH", "Near UNIBEN main gate and UBTH"),
    BeninCityArea("Airport Road", "Prime central location, reliable power", "Main road axis, good light"),
    BeninCityArea("Ikpoba Hill", "High elevation, scenic views, commercial", "Up hill area with busy markets"),
    BeninCityArea("Ring Road", "Heart of Benin City near King's Square", "Town center near Oba Market"),
    BeninCityArea("Ekenwan Road", "Affordable student and family living", "Good area for students and family"),
    BeninCityArea("Sapele Road", "Serene living along bypass and Country Home", "Cool area with fine compounds"),
    BeninCityArea("Aduwawa", "Fast developing residential area", "Developing area with modern houses"),
    BeninCityArea("Upper Sakponba", "Vibrant community with affordable rents", "Lively area with cheap rent")
  )

  // Dynamic list of properties connecting tenants with landlords through Dwello Homes
  val dynamicProperties = mutableStateListOf<PropertyItem>(
    PropertyItem(
      id = "prop_1",
      titleEn = "Standard Self-Contained Apartment",
      titlePidgin = "Sharp Self-Con Apartment",
      categoryEn = "Self-Con",
      categoryPidgin = "Self-Con",
      area = "Uselu / Ugbowo",
      priceNaira = 250000,
      priceFormatted = "₦250,000",
      bedrooms = 1,
      bathrooms = 1,
      isGated = true,
      hasBorehole = true,
      hasPrepaidMeter = true,
      landlordName = "Mr. Osasere",
      landlordRoleEn = "Landlord (Direct)",
      landlordRolePidgin = "Direct House Owner",
      phoneContact = "0800 393 556",
      descriptionEn = "Tiled room with personal kitchen, running water and prepaid meter. Gated compound with security gate. Very close to UNIBEN main gate.",
      descriptionPidgin = "Tiled room with personal kitchen, running tap water and personal prepaid meter. Fenced compound with gate. Very close to UNIBEN school gate.",
      neighborhoodNote = "Near Uselu Market axis",
      imageResId = R.drawable.img_benin_house_exterior,
      isDemonstrationData = true
    ),
    PropertyItem(
      id = "prop_2",
      titleEn = "Spacious 2-Bedroom Flat",
      titlePidgin = "Big 2-Bedroom Flat",
      categoryEn = "2-Bedroom Flat",
      categoryPidgin = "2-Bedroom Flat",
      area = "GRA",
      priceNaira = 650000,
      priceFormatted = "₦650,000",
      bedrooms = 2,
      bathrooms = 2,
      isGated = true,
      hasBorehole = true,
      hasPrepaidMeter = true,
      landlordName = "Chief Ighodaro",
      landlordRoleEn = "Property Owner",
      landlordRolePidgin = "House Owner",
      phoneContact = "0800 393 556",
      descriptionEn = "Newly painted 2-bedroom flat with POP ceiling, modern wardrobe, water heater, ample car parking space in a secure, serene GRA compound.",
      descriptionPidgin = "New painted 2-bedroom with fine POP ceiling, big wardrobe, water heater and enough space to pack motor inside secure GRA compound.",
      neighborhoodNote = "Off Boundary Road, GRA",
      imageResId = R.drawable.img_benin_house_interior,
      isDemonstrationData = true
    ),
    PropertyItem(
      id = "prop_3",
      titleEn = "Executive 3-Bedroom Flat",
      titlePidgin = "Fine 3-Bedroom Flat",
      categoryEn = "3-Bedroom Flat",
      categoryPidgin = "3-Bedroom Flat",
      area = "Airport Road",
      priceNaira = 850000,
      priceFormatted = "₦850,000",
      bedrooms = 3,
      bathrooms = 3,
      isGated = true,
      hasBorehole = true,
      hasPrepaidMeter = true,
      landlordName = "Madam Ngozi",
      landlordRoleEn = "Landlady",
      landlordRolePidgin = "House Owner",
      phoneContact = "0800 393 556",
      descriptionEn = "All rooms en-suite, spacious sitting room with dining section, modern kitchen cabinets, 24/7 borehole water, quiet residential street.",
      descriptionPidgin = "All rooms get personal toilet and bath, big parlor with dining, fine kitchen cabinets, steady borehole water, quiet street.",
      neighborhoodNote = "Off Oko Central, Airport Road",
      imageResId = R.drawable.img_benin_house_exterior,
      isDemonstrationData = true
    ),
    PropertyItem(
      id = "prop_4",
      titleEn = "Decent Single Room with Shared Amenities",
      titlePidgin = "Clean Single Room (Face-Me)",
      categoryEn = "Single Room",
      categoryPidgin = "Single Room",
      area = "Ekenwan Road",
      priceNaira = 95000,
      priceFormatted = "₦95,000",
      bedrooms = 1,
      bathrooms = 1,
      isGated = true,
      hasBorehole = true,
      hasPrepaidMeter = false,
      landlordName = "Baba Monday",
      landlordRoleEn = "Landlord",
      landlordRolePidgin = "Landlord",
      phoneContact = "0800 393 556",
      descriptionEn = "Clean cemented single room, fenced compound, well-maintained shared borehole and clean bathroom. Affordable and calm environment.",
      descriptionPidgin = "Clean room, fenced compound with gate, clean shared tap water and toilet. Cheap rent and calm environment.",
      neighborhoodNote = "Near Ekenwan Barracks",
      imageResId = R.drawable.img_benin_house_interior,
      isDemonstrationData = true
    ),
    PropertyItem(
      id = "prop_5",
      titleEn = "Commercial Roadside Shop / Office",
      titlePidgin = "Roadside Shop / Store",
      categoryEn = "Commercial Shop",
      categoryPidgin = "Shop",
      area = "Ikpoba Hill",
      priceNaira = 350000,
      priceFormatted = "₦350,000",
      bedrooms = 0,
      bathrooms = 1,
      isGated = false,
      hasBorehole = true,
      hasPrepaidMeter = true,
      landlordName = "Mr. Ehis",
      landlordRoleEn = "Property Owner",
      landlordRolePidgin = "House Owner",
      phoneContact = "0800 393 556",
      descriptionEn = "High-traffic roadside shop with rolling iron shutter, personal electricity meter, good for boutique, pharmacy, or provisions store.",
      descriptionPidgin = "Shop wey dey face main road, rolling iron door, personal meter, very good for boutique, pharmacy or provision store.",
      neighborhoodNote = "Ikpoba Hill main expressway",
      imageResId = R.drawable.img_benin_house_exterior,
      isDemonstrationData = true
    )
  )

  fun addProperty(item: PropertyItem) {
    dynamicProperties.add(0, item)
  }
}
