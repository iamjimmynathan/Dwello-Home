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

  fun searchPlaceholder(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Search area or location"
    AppLanguage.PIDGIN -> "Search area or street"
  }

  fun initialLocation(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Benin City, Edo State"
    AppLanguage.PIDGIN -> "Benin City, Edo State"
  }

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

  fun perYear(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "/ year"
    AppLanguage.PIDGIN -> "/ per year"
  }

  fun sendEnquiry(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Send Enquiry"
    AppLanguage.PIDGIN -> "Send Enquiry"
  }

  fun bookInspection(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Book Inspection"
    AppLanguage.PIDGIN -> "Book Inspection"
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
    AppLanguage.ENGLISH -> "Fill this form. Dwello Homes will connect verified tenants directly with you."
    AppLanguage.PIDGIN -> "Fill this form. Dwello Homes go connect tenants to your house direct."
  }

  // Language switch indicator
  fun languageSwitchLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Language: English"
    AppLanguage.PIDGIN -> "Language: Pidgin"
  }
}
