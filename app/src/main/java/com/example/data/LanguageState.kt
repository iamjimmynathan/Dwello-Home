package com.example.data

enum class AppLanguage {
  ENGLISH,
  PIDGIN
}

object DwelloStrings {
  fun appName(): String = "DWELLO HOMES"

  fun homeWelcome(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Find your next home."
    AppLanguage.PIDGIN -> "Find your next house."
  }

  fun homeSubtitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Find better houses. Connect with trusted landlords."
    AppLanguage.PIDGIN -> "Find correct house. Connect with legit landlords."
  }

  // Find a house screen headers
  fun findScreenTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Find your next home"
    AppLanguage.PIDGIN -> "Find your next house"
  }

  fun findScreenSubtitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Let's find a place you'll love."
    AppLanguage.PIDGIN -> "Make we find correct place wey you go love."
  }

  fun searchAreaPlaceholder(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Search by area or neighbourhood"
    AppLanguage.PIDGIN -> "Search by area or street"
  }

  fun defaultLocation(lang: AppLanguage): String = "Benin City, Edo State"

  fun searchPlaceholder(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Search area or location"
    AppLanguage.PIDGIN -> "Search area or street"
  }

  fun initialLocation(lang: AppLanguage): String = "Benin City, Edo State"

  fun findHouseButton(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "FIND A HOUSE"
    AppLanguage.PIDGIN -> "FIND HOUSE"
  }

  fun findHouseSubtext(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Flats, Self-con, Single rooms & Shops"
    AppLanguage.PIDGIN -> "Flats, Self-con, Single room & Shops"
  }

  fun postHouseButton(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "POST A HOUSE"
    AppLanguage.PIDGIN -> "POST HOUSE"
  }

  fun postHouseSubtext(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "For landlords & property owners"
    AppLanguage.PIDGIN -> "For landlords & house owners"
  }

  fun needHelpTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Need help? Contact Dwello Homes."
    AppLanguage.PIDGIN -> "You need help? Reach Dwello Homes."
  }

  fun needHelpSubtext(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "We connect tenants and landlords, and coordinate house inspections in Benin City"
    AppLanguage.PIDGIN -> "We dey connect tenants and landlords, and arrange house inspection for Benin"
  }

  fun callDirect(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Call Dwello"
    AppLanguage.PIDGIN -> "Call Dwello"
  }

  fun chatWhatsapp(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "WhatsApp Dwello"
    AppLanguage.PIDGIN -> "Chat Dwello"
  }

  fun popularAreasTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Popular Areas in Benin City"
    AppLanguage.PIDGIN -> "Hot Areas for Benin City"
  }

  fun featuredTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Available Rentals in Benin City"
    AppLanguage.PIDGIN -> "Houses Wey Dey Available Now"
  }

  fun viewAll(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "See All Houses"
    AppLanguage.PIDGIN -> "See All House"
  }

  fun viewDetails(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "VIEW DETAILS"
    AppLanguage.PIDGIN -> "VIEW DETAILS"
  }

  fun perYear(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "/ year"
    AppLanguage.PIDGIN -> "/ per year"
  }

  fun perMonth(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "/ month"
    AppLanguage.PIDGIN -> "/ per month"
  }

  fun sendEnquiry(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Send Enquiry"
    AppLanguage.PIDGIN -> "Send Enquiry"
  }

  fun sendEnquiryToDwello(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "SEND ENQUIRY TO DWELLO HOMES"
    AppLanguage.PIDGIN -> "SEND ENQUIRY TO DWELLO HOMES"
  }

  fun bookInspection(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Book Inspection"
    AppLanguage.PIDGIN -> "Book Inspection"
  }

  // Filter labels
  fun filterButton(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Filters"
    AppLanguage.PIDGIN -> "Filter"
  }

  fun filterDialogTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Filter Houses"
    AppLanguage.PIDGIN -> "Filter House"
  }

  fun propertyTypeLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Property Type"
    AppLanguage.PIDGIN -> "Type of House"
  }

  fun rentBudgetLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Rent Budget (₦)"
    AppLanguage.PIDGIN -> "Rent Budget (₦)"
  }

  fun minRentLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Min Rent"
    AppLanguage.PIDGIN -> "Smallest Rent"
  }

  fun maxRentLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Max Rent"
    AppLanguage.PIDGIN -> "Highest Rent"
  }

  fun rentalPeriodLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Rental Period"
    AppLanguage.PIDGIN -> "Payment Time"
  }

  fun periodYearly(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Yearly"
    AppLanguage.PIDGIN -> "Per Year"
  }

  fun periodMonthly(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Monthly"
    AppLanguage.PIDGIN -> "Per Month"
  }

  fun periodAll(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "All Periods"
    AppLanguage.PIDGIN -> "All"
  }

  fun applyFilters(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "APPLY FILTER"
    AppLanguage.PIDGIN -> "APPLY FILTER"
  }

  fun resetFilters(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "RESET FILTERS"
    AppLanguage.PIDGIN -> "CLEAR ALL"
  }

  // Navigation tab labels
  fun navHome(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Home"
    AppLanguage.PIDGIN -> "Home"
  }

  fun navFind(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Find Houses"
    AppLanguage.PIDGIN -> "Find Houses"
  }

  fun navPost(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Post House"
    AppLanguage.PIDGIN -> "Post House"
  }

  fun navMessages(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Enquiries"
    AppLanguage.PIDGIN -> "Enquiries"
  }

  fun navProfile(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Profile"
    AppLanguage.PIDGIN -> "Profile"
  }

  // Post house form strings
  fun postHouseHeader(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "List Property with Dwello Homes"
    AppLanguage.PIDGIN -> "Put Your House for Dwello Homes"
  }

  fun postHouseInstruction(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Fill this form. All submissions undergo Dwello Homes verification before approval."
    AppLanguage.PIDGIN -> "Fill this form. Dwello Homes go check your house well before e approve am."
  }

  // Approval Status Labels
  fun statusPending(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Pending Approval"
    AppLanguage.PIDGIN -> "Under Review (Pending)"
  }

  fun statusApproved(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Approved & Live"
    AppLanguage.PIDGIN -> "Approved (Live)"
  }

  fun statusRejected(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Rejected"
    AppLanguage.PIDGIN -> "Rejected"
  }

  fun statusUnavailable(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Unavailable"
    AppLanguage.PIDGIN -> "Not Available"
  }

  // Demonstration Notice
  fun demoNotice(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Sample Demonstration Listing — Not a Real Property"
    AppLanguage.PIDGIN -> "Sample Demo House — No Be Real House"
  }

  // Language switch indicator
  fun languageSwitchLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Language: English"
    AppLanguage.PIDGIN -> "Language: Pidgin"
  }
}
