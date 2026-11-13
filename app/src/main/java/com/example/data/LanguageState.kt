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

  // Landlord Dashboard Stage 3A Strings
  fun landlordDashboardTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Landlord Dashboard"
    AppLanguage.PIDGIN -> "Landlord Office"
  }

  fun landlordDashboardSubtitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Manage listings, track approval status, and view enquiries"
    AppLanguage.PIDGIN -> "Manage your houses, see approval status, and check tenant enquiries"
  }

  fun statTotalProperties(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Total Properties"
    AppLanguage.PIDGIN -> "Total Houses"
  }

  fun statApprovedProperties(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Approved & Live"
    AppLanguage.PIDGIN -> "Approved (Live)"
  }

  fun statPendingApproval(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Pending Approval"
    AppLanguage.PIDGIN -> "Under Review"
  }

  fun statRejectedProperties(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Rejected"
    AppLanguage.PIDGIN -> "Rejected"
  }

  fun statNewEnquiries(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "New Enquiries"
    AppLanguage.PIDGIN -> "New Enquiries"
  }

  fun addNewPropertyButton(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "ADD NEW PROPERTY"
    AppLanguage.PIDGIN -> "PUT NEW HOUSE"
  }

  fun myPropertiesHeading(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "My Properties"
    AppLanguage.PIDGIN -> "My Houses"
  }

  fun filterAll(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "All"
    AppLanguage.PIDGIN -> "All"
  }

  fun editListingButton(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Edit Listing"
    AppLanguage.PIDGIN -> "Edit Details"
  }

  fun markUnavailableButton(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Mark Unavailable"
    AppLanguage.PIDGIN -> "Mark Unavailable"
  }

  fun markAvailableButton(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Mark as Available"
    AppLanguage.PIDGIN -> "Make am Available"
  }

  fun rejectionReasonTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Dwello Homes Moderation Feedback:"
    AppLanguage.PIDGIN -> "Wetin Dwello Homes Admin Talk:"
  }

  fun rejectionFixHelp(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Tap 'Edit Listing' to rectify the items noted and resubmit for verification."
    AppLanguage.PIDGIN -> "Touch 'Edit Details' to fix the things wey dem talk and resubmit am."
  }

  fun noSelfApprovalNotice(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Notice: In accordance with Dwello Homes safety standards, all listings are independently verified by our admin team before publication. Landlords cannot approve their own listings."
    AppLanguage.PIDGIN -> "Notice: For safety sake, Dwello Homes team must verify house details before e show to tenants. Landlord no fit approve im own house by imself."
  }

  fun privatePhoneProtectedNotice(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Privacy Protected: Your personal phone number is never exposed to tenants. Inspections and enquiries are coordinated through Dwello Homes."
    AppLanguage.PIDGIN -> "Privacy Protected: Tenants no go see your phone number. Dwello Homes dey coordinate all inspections."
  }

  // --- STAGE 3B: POST A HOUSE WORKFLOW STRINGS ---
  fun postHouseFormTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Post a Property"
    AppLanguage.PIDGIN -> "Put House for Rent"
  }

  fun postHouseFormSubtitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Fill out the property details below. All submissions are reviewed by Dwello Homes before going live to tenants."
    AppLanguage.PIDGIN -> "Fill the house details below. Dwello Homes go check am well before tenants go see am."
  }

  // Photos
  fun sectionPhotosTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "1. Property Photos"
    AppLanguage.PIDGIN -> "1. House Pictures"
  }

  fun photosInstruction(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Select photos from your device gallery. Clear photos of exterior, living room, and bedrooms help tenants inspect faster."
    AppLanguage.PIDGIN -> "Pick clean pictures from your phone gallery. Pictures of compound gate, parlor, and rooms go help tenant quick."
  }

  fun selectPhotosBtn(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Select Photos from Gallery"
    AppLanguage.PIDGIN -> "Pick Pictures from Phone"
  }

  fun noPhotosSelected(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "No photos selected yet. Please select at least 1 photo."
    AppLanguage.PIDGIN -> "You never pick picture. Pick at least 1 picture for the house."
  }

  fun selectedPhotosCount(lang: AppLanguage, count: Int): String = when (lang) {
    AppLanguage.ENGLISH -> "Selected Photos ($count)"
    AppLanguage.PIDGIN -> "Pictures Wey You Don Pick ($count)"
  }

  fun localPhotoNotice(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Honest Notice: Selected photos are previewed locally on your device for this submission."
    AppLanguage.PIDGIN -> "Honest Notice: Pictures dey save for your phone here for now."
  }

  // Property Details
  fun sectionDetailsTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "2. Property Details"
    AppLanguage.PIDGIN -> "2. House Details"
  }

  fun titleLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Property Title"
    AppLanguage.PIDGIN -> "House Title"
  }

  fun titlePlaceholder(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "e.g. Spacious 2-Bedroom Flat in Ugbowo"
    AppLanguage.PIDGIN -> "e.g. Big 2-Bedroom Flat for Ugbowo"
  }

  fun errorTitleRequired(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Please enter a property title"
    AppLanguage.PIDGIN -> "Please write title for the house"
  }

  fun selectTypeLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Property Type"
    AppLanguage.PIDGIN -> "Type of House"
  }

  fun errorTypeRequired(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Please select a property type"
    AppLanguage.PIDGIN -> "Please pick the type of house"
  }

  fun bedroomsCountLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Number of Bedrooms"
    AppLanguage.PIDGIN -> "How Many Bedrooms"
  }

  fun bathroomsCountLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Number of Bathrooms"
    AppLanguage.PIDGIN -> "How Many Bathrooms"
  }

  fun rentAmountLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Rent Amount (₦)"
    AppLanguage.PIDGIN -> "Rent Money (₦)"
  }

  fun rentAmountPlaceholder(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "e.g. 450000"
    AppLanguage.PIDGIN -> "e.g. 450000"
  }

  fun errorRentRequired(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Rent amount must be greater than ₦0"
    AppLanguage.PIDGIN -> "Rent money must be more than ₦0"
  }

  fun paymentPeriodLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Preferred Payment Period"
    AppLanguage.PIDGIN -> "Payment Time"
  }

  fun periodYearlyOption(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Yearly (Per Year)"
    AppLanguage.PIDGIN -> "Per Year"
  }

  fun periodMonthlyOption(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Monthly (Per Month)"
    AppLanguage.PIDGIN -> "Per Month"
  }

  fun descriptionLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Property Description"
    AppLanguage.PIDGIN -> "Talk About The House"
  }

  fun descriptionPlaceholder(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Describe the rooms, compound environment, road accessibility, and conditions..."
    AppLanguage.PIDGIN -> "Talk about the rooms, compound, light and water situation..."
  }

  fun errorDescriptionRequired(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Please provide a property description"
    AppLanguage.PIDGIN -> "Please write small description for the house"
  }

  // Location
  fun sectionLocationTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "3. Property Location in Benin City"
    AppLanguage.PIDGIN -> "3. Where House Dey for Benin City"
  }

  fun neighbourhoodLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Select Neighbourhood"
    AppLanguage.PIDGIN -> "Pick Area"
  }

  fun errorNeighbourhoodRequired(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Please select a neighbourhood in Benin City"
    AppLanguage.PIDGIN -> "Please pick the area in Benin City"
  }

  fun addressLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Full Address or Nearby Landmark"
    AppLanguage.PIDGIN -> "Full Address or Nearest Bus Stop"
  }

  fun addressPlaceholder(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "e.g. 15 Boundary Road, off Sapele Road / Near UBTH Gate"
    AppLanguage.PIDGIN -> "e.g. 15 Boundary Road, near UBTH Gate"
  }

  fun locationNotice(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Address is used by Dwello coordinators to schedule physical inspections. No automated GPS verification claimed."
    AppLanguage.PIDGIN -> "Address dey help Dwello coordinator to arrange physical inspection. No fake GPS dey here."
  }

  // Facilities
  fun sectionFacilitiesTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "4. Property Facilities"
    AppLanguage.PIDGIN -> "4. Things Wey Dey Compound"
  }

  fun facilitiesInstruction(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Select all available facilities in the compound:"
    AppLanguage.PIDGIN -> "Check all the things wey dey inside compound:"
  }

  fun additionalFacilitiesLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Additional Facilities Description"
    AppLanguage.PIDGIN -> "Other Facilities Description"
  }

  fun additionalFacilitiesPlaceholder(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "e.g. Dedicated prepaid meter, borehole with treatment, night watchman"
    AppLanguage.PIDGIN -> "e.g. Personal prepaid meter, steady water, night security"
  }

  // Submit and Dialogs
  fun submitButtonText(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "SUBMIT PROPERTY FOR REVIEW"
    AppLanguage.PIDGIN -> "SUBMIT HOUSE FOR REVIEW"
  }

  fun discardDialogTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Discard Listing?"
    AppLanguage.PIDGIN -> "Cancel This Post?"
  }

  fun discardDialogMessage(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "You have unsaved changes in this property form. Do you want to discard your draft and return?"
    AppLanguage.PIDGIN -> "You get details wey you never submit. You sure say you wan cancel and leave?"
  }

  fun discardConfirmBtn(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Discard & Exit"
    AppLanguage.PIDGIN -> "Cancel & Leave"
  }

  fun discardCancelBtn(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Keep Editing"
    AppLanguage.PIDGIN -> "Continue Form"
  }

  fun successDialogTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Submission Successful"
    AppLanguage.PIDGIN -> "House Don Submit"
  }

  fun successDialogMessage(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Your property has been submitted successfully. Dwello Homes will review it before it appears to tenants."
    AppLanguage.PIDGIN -> "Your property don submit successfully. Dwello Homes go check and verify am before tenants go see am."
  }

  fun successDialogPendingNote(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Approval Status: PENDING APPROVAL. Landlords cannot approve their own listings."
    AppLanguage.PIDGIN -> "Approval Status: PENDING APPROVAL. Landlords no fit approve their own houses."
  }

  fun viewInDashboardBtn(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "VIEW IN MY PROPERTIES"
    AppLanguage.PIDGIN -> "CHECK FOR MY HOUSES"
  }

  // --- STAGE 4: WELCOME, ACCOUNT SELECTION, REGISTRATION, LOGIN & RECOVERY STRINGS ---
  fun welcomeHeading(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Find a place to call home."
    AppLanguage.PIDGIN -> "Find better place wey you go call home."
  }

  fun welcomeSupportingText(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Discover available houses or list your property with Dwello Homes."
    AppLanguage.PIDGIN -> "Find house wey dey available or put your own house for Dwello Homes."
  }

  fun getStartedBtn(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Get Started"
    AppLanguage.PIDGIN -> "Start Now"
  }

  fun loginBtn(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Login"
    AppLanguage.PIDGIN -> "Login"
  }

  fun welcomeDemoBadge(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Benin City, Edo State • Tenant & Landlord Platform"
    AppLanguage.PIDGIN -> "Benin City, Edo State • For Tenants & Landlords"
  }

  fun continueDemoGuest(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Browse Houses in Demo Preview →"
    AppLanguage.PIDGIN -> "Check Houses for Demo Preview →"
  }

  // Account Type Selection
  fun accountTypeTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Choose Your Account Type"
    AppLanguage.PIDGIN -> "Pick Your Account Type"
  }

  fun accountTypeSubtitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Select how you want to use Dwello Homes in Benin City."
    AppLanguage.PIDGIN -> "Tell us how you wan use Dwello Homes for Benin City."
  }

  fun tenantAccountLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "TENANT"
    AppLanguage.PIDGIN -> "TENANT"
  }

  fun tenantAccountHeading(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "I'm looking for a house."
    AppLanguage.PIDGIN -> "I dey find house to rent."
  }

  fun tenantAccountDescription(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Find available houses around Benin City and contact Dwello Homes."
    AppLanguage.PIDGIN -> "Find available houses around Benin City and message Dwello Homes."
  }

  fun landlordAccountLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "LANDLORD"
    AppLanguage.PIDGIN -> "LANDLORD"
  }

  fun landlordAccountHeading(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "I want to rent out my property."
    AppLanguage.PIDGIN -> "I wan put my house for rent."
  }

  fun landlordAccountDescription(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "List your property, manage availability, and receive enquiries through Dwello Homes."
    AppLanguage.PIDGIN -> "Submit your house, manage availability, and collect enquiries through Dwello Homes."
  }

  fun continueToRegistrationBtn(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Continue to Registration"
    AppLanguage.PIDGIN -> "Continue to Register"
  }

  fun noAgentNotice(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Dwello Homes connects Tenants and Landlords directly. There is no Agent role on this platform."
    AppLanguage.PIDGIN -> "Dwello Homes dey connect Tenants and Landlords direct. No Agent account dey here."
  }

  // Registration
  fun registerTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Create Your Account"
    AppLanguage.PIDGIN -> "Open Your Account"
  }

  fun registerSubtitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Enter your details below to register with Dwello Homes."
    AppLanguage.PIDGIN -> "Put your details below to join Dwello Homes."
  }

  fun selectedRoleBannerLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Account Type:"
    AppLanguage.PIDGIN -> "Account Type:"
  }

  fun changeRoleText(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Change"
    AppLanguage.PIDGIN -> "Change am"
  }

  fun fullNameLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Full Name"
    AppLanguage.PIDGIN -> "Your Full Name"
  }

  fun fullNamePlaceholder(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "e.g. Osaro Ighodaro"
    AppLanguage.PIDGIN -> "e.g. Osaro Ighodaro"
  }

  fun phoneNumberLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Nigerian Phone Number"
    AppLanguage.PIDGIN -> "Nigeria Phone Number"
  }

  fun phoneNumberPlaceholder(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "e.g. 0803 123 4567"
    AppLanguage.PIDGIN -> "e.g. 0803 123 4567"
  }

  fun emailOptionalLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Email Address (Optional)"
    AppLanguage.PIDGIN -> "Email Address (No Be Must)"
  }

  fun emailPlaceholder(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "e.g. name@example.com"
    AppLanguage.PIDGIN -> "e.g. name@example.com"
  }

  fun passwordLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Password"
    AppLanguage.PIDGIN -> "Password"
  }

  fun passwordHelper(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Minimum 8 characters"
    AppLanguage.PIDGIN -> "Make e reach 8 characters or more"
  }

  fun confirmPasswordLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Confirm Password"
    AppLanguage.PIDGIN -> "Write Password Again"
  }

  fun acceptTermsLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "I agree to the Dwello Homes Terms and Conditions and Privacy Policy"
    AppLanguage.PIDGIN -> "I agree to Dwello Homes Terms and Conditions and Privacy Policy"
  }

  fun createAccountBtn(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Create Account"
    AppLanguage.PIDGIN -> "Create Account"
  }

  fun alreadyHaveAccountPrompt(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Already have an account? Login"
    AppLanguage.PIDGIN -> "You don get account already? Login"
  }

  // Login
  fun loginTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Welcome Back"
    AppLanguage.PIDGIN -> "Welcome Back"
  }

  fun loginSubtitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Sign in with your Nigerian phone number and password."
    AppLanguage.PIDGIN -> "Enter with your Nigeria phone number and password."
  }

  fun forgotPasswordLink(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Forgot Password?"
    AppLanguage.PIDGIN -> "You Forget Password?"
  }

  fun dontHaveAccountPrompt(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Don't have an account? Register"
    AppLanguage.PIDGIN -> "You never get account? Register"
  }

  // Forgot Password
  fun forgotPasswordTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Forgot Password"
    AppLanguage.PIDGIN -> "Forget Password"
  }

  fun forgotPasswordSubtitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Enter your registered Nigerian phone number to prepare password recovery."
    AppLanguage.PIDGIN -> "Put the Nigeria phone number wey you take register."
  }

  fun continueBtn(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Continue"
    AppLanguage.PIDGIN -> "Continue"
  }

  fun backToLoginLink(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Back to Login"
    AppLanguage.PIDGIN -> "Go Back to Login"
  }

  fun otpBackendPendingNotice(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Password recovery will become available after connecting the secure backend and SMS/OTP verification service. No OTP or verification code has been sent in this frontend stage."
    AppLanguage.PIDGIN -> "Password recovery go start to work when we connect secure backend and SMS/OTP service. No OTP code don send for this frontend stage."
  }

  // Validation Errors
  fun authErrorNameRequired(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Full name is required."
    AppLanguage.PIDGIN -> "Please write your full name."
  }

  fun authErrorNameTooShort(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Please enter at least 2 characters for your name."
    AppLanguage.PIDGIN -> "Make your name reach 2 letters or more."
  }

  fun authErrorPhoneRequired(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Nigerian phone number is required."
    AppLanguage.PIDGIN -> "Please put your Nigeria phone number."
  }

  fun authErrorPhoneInvalid(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Enter a valid Nigerian mobile number (e.g. 0803 123 4567 or +234 803 123 4567)."
    AppLanguage.PIDGIN -> "Put correct Nigeria phone number (like 0803 123 4567 or +234 803 123 4567)."
  }

  fun authErrorEmailInvalid(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Please enter a valid email address or leave it blank."
    AppLanguage.PIDGIN -> "Put correct email address or leave am empty."
  }

  fun authErrorPasswordRequired(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Password is required."
    AppLanguage.PIDGIN -> "Please put password."
  }

  fun authErrorPasswordLength(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Password must be at least 8 characters long."
    AppLanguage.PIDGIN -> "Password must reach at least 8 characters."
  }

  fun authErrorConfirmPasswordRequired(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Please confirm your password."
    AppLanguage.PIDGIN -> "Please write your password again."
  }

  fun authErrorPasswordMismatch(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Passwords do not match."
    AppLanguage.PIDGIN -> "The two passwords no match."
  }

  fun authErrorTermsRequired(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Please accept the Terms and Conditions and Privacy Policy to continue."
    AppLanguage.PIDGIN -> "Please tick the Terms and Conditions box before you continue."
  }

  fun authErrorRoleRequired(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Please select either Tenant or Landlord account type."
    AppLanguage.PIDGIN -> "Please pick Tenant or Landlord account type."
  }

  // Honest Demonstration Notices
  fun demoAuthBannerText(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Demonstration UI Mode: Real authentication, password hashing, duplicate-phone checks, and SMS/OTP verification will be connected in the backend stage."
    AppLanguage.PIDGIN -> "Demo UI Mode: Real login, password security, duplicate-number check, and OTP go connect when backend ready."
  }

  fun registrationDemoDialogTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Frontend Registration Preview"
    AppLanguage.PIDGIN -> "Registration Form Preview"
  }

  fun registrationDemoDialogBody(lang: AppLanguage, normalizedPhone: String): String = when (lang) {
    AppLanguage.ENGLISH -> "Your inputs passed frontend validation (Normalized phone: $normalizedPhone).\n\nImportant: No real account has been created and your password was NOT stored. Real account creation, duplicate phone number checks, and OTP verification require backend integration."
    AppLanguage.PIDGIN -> "Your form details pass validation (Phone: $normalizedPhone).\n\nImportant: Real account never create yet and we no save your password. Real account creation and OTP go work when backend connect."
  }

  fun loginDemoDialogTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Frontend Login Preview"
    AppLanguage.PIDGIN -> "Login Form Preview"
  }

  fun loginDemoDialogBody(lang: AppLanguage, normalizedPhone: String): String = when (lang) {
    AppLanguage.ENGLISH -> "Phone ($normalizedPhone) and password format passed frontend validation.\n\nImportant: You are NOT authenticated yet because real authentication requires backend integration. Choose a role below to preview the post-login navigation."
    AppLanguage.PIDGIN -> "Phone ($normalizedPhone) and password format pass validation.\n\nImportant: Real authentication never happen yet until backend connect. Pick role below to test navigation."
  }

  fun roleRestrictedTitle(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "Role Access Notice"
    AppLanguage.PIDGIN -> "Account Role Notice"
  }

  fun roleRestrictedTenantToLandlord(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "This section is for Landlord accounts only. Tenants cannot access landlord property management or listing screens."
    AppLanguage.PIDGIN -> "Na only Landlord account fit open this place. Tenant no fit enter landlord dashboard or post house."
  }

  fun roleRestrictedLandlordToTenant(lang: AppLanguage): String = when (lang) {
    AppLanguage.ENGLISH -> "You are currently previewing a Landlord account. Switch to Tenant in Profile or Login as a Tenant to browse tenant search screens."
    AppLanguage.PIDGIN -> "You dey use Landlord account now. Change to Tenant for Profile if you wan find house."
  }
}
