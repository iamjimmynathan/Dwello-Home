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
    AppLanguage.ENGLISH -> "For landlords, agents & caretakers"
    AppLanguage.PIDGIN -> "For landlord, agent & caretaker"
  }

  fun needHelpTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Need help? Contact a landlord or agent."
    AppLanguage.PIDGIN -> "You need help? Call landlord or agent."
  }

  fun needHelpSubtext(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Direct phone line and WhatsApp support for Benin City residents"
    AppLanguage.PIDGIN -> "Call direct or chat for WhatsApp sharp sharp"
  }

  fun callDirect(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Call Help Desk"
    AppLanguage.PIDGIN -> "Call Us Direct"
  }

  fun chatWhatsapp(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "WhatsApp Agent"
    AppLanguage.PIDGIN -> "Chat for WhatsApp"
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

  fun tapToCall(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Call Landlord"
    AppLanguage.PIDGIN -> "Call Landlord"
  }

  fun tapToChat(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Message"
    AppLanguage.PIDGIN -> "Send Message"
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
    AppLanguage.ENGLISH -> "Messages"
    AppLanguage.PIDGIN -> "Messages"
  }

  fun navProfile(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Profile"
    AppLanguage.PIDGIN -> "Profile"
  }

  // Post house form strings
  fun postHouseHeader(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Post Your Property in Benin City"
    AppLanguage.PIDGIN -> "Put Your House for Benin City"
  }

  fun postHouseInstruction(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Fill this simple form. Tenants in Benin City will call you directly."
    AppLanguage.PIDGIN -> "Fill this simple form. Tenants for Benin will call your phone straight."
  }

  // Language switch indicator
  fun languageSwitchLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Language: English"
    AppLanguage.PIDGIN -> "Language: Pidgin"
  }
}
