package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AuthAccountRole
import com.example.data.AuthenticatedUser
import com.example.data.DwelloApiResult
import com.example.data.DwelloRepository
import com.example.data.EnquiryItem
import com.example.data.EnquiryStatus
import com.example.data.PropertyItem
import com.example.data.PropertyPhotoRecord
import com.example.data.PropertySubmissionResult
import com.example.data.RentalPeriod
import com.example.data.SelectedPhotoItem
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SessionStatus {
  RESTORING,
  UNAUTHENTICATED,
  AUTHENTICATED
}

sealed class AsyncResourceState<out T> {
  data object Idle : AsyncResourceState<Nothing>()
  data object Loading : AsyncResourceState<Nothing>()
  data class Success<T>(val data: T) : AsyncResourceState<T>()
  data class Error(val message: String) : AsyncResourceState<Nothing>()
}

data class DwelloUiState(
  val sessionStatus: SessionStatus = SessionStatus.RESTORING,
  val currentUser: AuthenticatedUser? = null,
  val authLoading: Boolean = false,
  val authError: String? = null,

  // Tenant Discovery & Details
  val approvedPropertiesState: AsyncResourceState<List<PropertyItem>> = AsyncResourceState.Idle,
  val favouriteIds: Set<String> = emptySet(),
  val favouriteActionIds: Set<String> = emptySet(),
  val favouriteError: String? = null,
  val selectedPropertyDetailState: AsyncResourceState<PropertyItem>? = null,

  // Tenant Enquiries
  val tenantEnquiriesState: AsyncResourceState<List<EnquiryItem>> = AsyncResourceState.Idle,
  val enquirySubmitting: Boolean = false,
  val enquirySubmitError: String? = null,
  val enquirySubmittedSuccess: Boolean = false,

  // Landlord Dashboard & Management
  val landlordPropertiesState: AsyncResourceState<List<PropertyItem>> = AsyncResourceState.Idle,
  val landlordEnquiriesState: AsyncResourceState<List<EnquiryItem>> = AsyncResourceState.Idle,
  val selectedLandlordPropertyDetailState: AsyncResourceState<PropertyItem>? = null,
  val landlordActionLoading: Boolean = false,
  val landlordActionMessage: String? = null,
  val landlordActionError: String? = null,

  // Landlord Post a House
  val postPropertyLoading: Boolean = false,
  val postPropertyUploadProgress: String? = null,
  val postPropertyError: String? = null,
  val postPropertySuccessId: String? = null,
  val pendingUploadPropertyId: String? = null,
  val pendingUploadPhotos: List<SelectedPhotoItem> = emptyList(),
  val pendingUploadNextDisplayOrder: Int = 0,
  val pendingUploadConfirmedImages: List<PropertyPhotoRecord> = emptyList()
)

class DwelloViewModel(
  private val repository: DwelloRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow(
    DwelloUiState(
      sessionStatus = if (repository.hasStoredSession()) {
        SessionStatus.RESTORING
      } else {
        SessionStatus.UNAUTHENTICATED
      }
    )
  )
  val uiState: StateFlow<DwelloUiState> = _uiState.asStateFlow()

  // Holds only server-confirmed PropertyPhotoRecord items returned from POST /api/properties/{id}/images
  private val confirmedServerImagesCache = mutableMapOf<String, List<PropertyPhotoRecord>>()

  init {
    if (repository.hasStoredSession()) {
      restoreSession()
    }
  }

  fun restoreSession() {
    if (!repository.hasStoredSession()) {
      _uiState.update {
        it.copy(
          sessionStatus = SessionStatus.UNAUTHENTICATED,
          currentUser = null
        )
      }
      return
    }

    _uiState.update { it.copy(sessionStatus = SessionStatus.RESTORING, authError = null) }
    viewModelScope.launch {
      when (val result = repository.restoreSession()) {
        is DwelloApiResult.Success -> {
          onAuthenticatedSuccess(result.data)
        }
        is DwelloApiResult.Error -> {
          repository.clearLocalSession()
          confirmedServerImagesCache.clear()
          _uiState.update {
            it.copy(
              sessionStatus = SessionStatus.UNAUTHENTICATED,
              currentUser = null,
              authError = if (result.isUnauthorized) null else result.message
            )
          }
        }
      }
    }
  }

  fun clearAuthError() {
    _uiState.update { it.copy(authError = null) }
  }

  fun registerAndLogin(
    fullName: String,
    normalizedPhone: String,
    email: String?,
    passwordChars: CharArray,
    role: AuthAccountRole
  ) {
    if (_uiState.value.authLoading) {
      passwordChars.fill('\u0000')
      return
    }
    _uiState.update { it.copy(authLoading = true, authError = null) }
    viewModelScope.launch {
      when (val result = repository.registerAndLogin(fullName, normalizedPhone, email, passwordChars, role)) {
        is DwelloApiResult.Success -> {
          onAuthenticatedSuccess(result.data)
        }
        is DwelloApiResult.Error -> {
          _uiState.update { it.copy(authLoading = false, authError = result.message) }
        }
      }
    }
  }

  fun login(
    normalizedPhone: String,
    passwordChars: CharArray
  ) {
    if (_uiState.value.authLoading) {
      passwordChars.fill('\u0000')
      return
    }
    _uiState.update { it.copy(authLoading = true, authError = null) }
    viewModelScope.launch {
      when (val result = repository.login(normalizedPhone, passwordChars)) {
        is DwelloApiResult.Success -> {
          onAuthenticatedSuccess(result.data)
        }
        is DwelloApiResult.Error -> {
          _uiState.update { it.copy(authLoading = false, authError = result.message) }
        }
      }
    }
  }

  fun logout() {
    _uiState.update { it.copy(authLoading = true) }
    viewModelScope.launch {
      repository.logout()
      confirmedServerImagesCache.clear()
      _uiState.value = DwelloUiState(
        sessionStatus = SessionStatus.UNAUTHENTICATED,
        currentUser = null,
        authLoading = false
      )
    }
  }

  private fun onAuthenticatedSuccess(user: AuthenticatedUser) {
    confirmedServerImagesCache.clear()
    _uiState.value = DwelloUiState(
      sessionStatus = SessionStatus.AUTHENTICATED,
      currentUser = user,
      authLoading = false,
      authError = null
    )
    when (user.role) {
      AuthAccountRole.TENANT -> loadTenantData()
      AuthAccountRole.LANDLORD -> loadLandlordData()
    }
  }

  private fun currentRole(): AuthAccountRole? {
    val state = _uiState.value
    return if (state.sessionStatus == SessionStatus.AUTHENTICATED) {
      state.currentUser?.role
    } else {
      null
    }
  }

  private fun mergeConfirmedImages(item: PropertyItem): PropertyItem {
    if (item.photoUris.isNotEmpty()) {
      confirmedServerImagesCache[item.id] = item.serverImages
      return item
    }
    val cached = confirmedServerImagesCache[item.id].orEmpty()
    return if (cached.isNotEmpty()) {
      val ordered = cached.sortedBy { it.displayOrder }
      item.copy(
        photoUris = ordered.map { it.imageUrl },
        serverImages = ordered
      )
    } else {
      item
    }
  }

  fun loadTenantData() {
    if (!com.example.data.RoleNavigationRouter.canBrowseApprovedProperties(currentRole())) return
    loadApprovedProperties()
    loadFavourites()
    loadTenantEnquiries()
  }

  fun loadApprovedProperties() {
    if (!com.example.data.RoleNavigationRouter.canBrowseApprovedProperties(currentRole())) return
    _uiState.update { it.copy(approvedPropertiesState = AsyncResourceState.Loading) }
    viewModelScope.launch {
      when (val result = repository.getApprovedProperties()) {
        is DwelloApiResult.Success -> {
          _uiState.update { it.copy(approvedPropertiesState = AsyncResourceState.Success(result.data)) }
        }
        is DwelloApiResult.Error -> {
          if (handleUnauthorizedIfNeeded(result)) return@launch
          _uiState.update { it.copy(approvedPropertiesState = AsyncResourceState.Error(result.message)) }
        }
      }
    }
  }

  fun loadFavourites() {
    if (!com.example.data.RoleNavigationRouter.canBrowseApprovedProperties(currentRole())) return
    viewModelScope.launch {
      when (val result = repository.getFavourites()) {
        is DwelloApiResult.Success -> {
          val ids = result.data.map { it.id }.toSet()
          _uiState.update { it.copy(favouriteIds = ids, favouriteError = null) }
        }
        is DwelloApiResult.Error -> {
          if (handleUnauthorizedIfNeeded(result)) return@launch
          _uiState.update { it.copy(favouriteError = result.message) }
        }
      }
    }
  }

  fun toggleFavourite(propertyId: String) {
    if (!com.example.data.RoleNavigationRouter.canBrowseApprovedProperties(currentRole())) return
    if (_uiState.value.favouriteActionIds.contains(propertyId)) return
    val currentlyFav = _uiState.value.favouriteIds.contains(propertyId)
    _uiState.update {
      it.copy(
        favouriteActionIds = it.favouriteActionIds + propertyId,
        favouriteError = null
      )
    }
    viewModelScope.launch {
      val result = if (currentlyFav) {
        repository.removeFavourite(propertyId)
      } else {
        repository.addFavourite(propertyId)
      }
      when (result) {
        is DwelloApiResult.Success -> {
          _uiState.update { state ->
            val nextFavs = if (currentlyFav) {
              state.favouriteIds - propertyId
            } else {
              state.favouriteIds + propertyId
            }
            state.copy(
              favouriteIds = nextFavs,
              favouriteActionIds = state.favouriteActionIds - propertyId
            )
          }
        }
        is DwelloApiResult.Error -> {
          if (handleUnauthorizedIfNeeded(result)) return@launch
          _uiState.update { state ->
            state.copy(
              favouriteActionIds = state.favouriteActionIds - propertyId,
              favouriteError = result.message
            )
          }
        }
      }
    }
  }

  fun openPropertyDetails(propertySummary: PropertyItem) {
    if (!com.example.data.RoleNavigationRouter.canAccessPropertyDetails(currentRole())) return
    _uiState.update {
      it.copy(selectedPropertyDetailState = AsyncResourceState.Loading)
    }
    viewModelScope.launch {
      when (val result = repository.getPropertyDetails(propertySummary.id)) {
        is DwelloApiResult.Success -> {
          _uiState.update {
            it.copy(selectedPropertyDetailState = AsyncResourceState.Success(result.data))
          }
        }
        is DwelloApiResult.Error -> {
          if (handleUnauthorizedIfNeeded(result)) return@launch
          _uiState.update {
            it.copy(selectedPropertyDetailState = AsyncResourceState.Error(result.message))
          }
        }
      }
    }
  }

  fun closePropertyDetails() {
    _uiState.update { it.copy(selectedPropertyDetailState = null) }
  }

  fun openLandlordPropertyDetails(propertySummary: PropertyItem) {
    if (!com.example.data.RoleNavigationRouter.canAccessLandlordManagement(currentRole())) return
    _uiState.update {
      it.copy(selectedLandlordPropertyDetailState = AsyncResourceState.Loading)
    }
    viewModelScope.launch {
      when (val result = repository.getPropertyDetails(propertySummary.id)) {
        is DwelloApiResult.Success -> {
          val resolved = mergeConfirmedImages(result.data)
          _uiState.update {
            it.copy(selectedLandlordPropertyDetailState = AsyncResourceState.Success(resolved))
          }
        }
        is DwelloApiResult.Error -> {
          if (handleUnauthorizedIfNeeded(result)) return@launch
          // Pending/rejected listings may not always be returned by public detail endpoint; fall back cleanly
          _uiState.update {
            it.copy(
              selectedLandlordPropertyDetailState = AsyncResourceState.Success(
                mergeConfirmedImages(propertySummary)
              )
            )
          }
        }
      }
    }
  }

  fun closeLandlordPropertyDetails() {
    _uiState.update { it.copy(selectedLandlordPropertyDetailState = null) }
  }

  fun loadTenantEnquiries() {
    if (!com.example.data.RoleNavigationRouter.canSubmitPropertyEnquiry(currentRole())) return
    _uiState.update { it.copy(tenantEnquiriesState = AsyncResourceState.Loading) }
    viewModelScope.launch {
      when (val result = repository.getTenantEnquiries()) {
        is DwelloApiResult.Success -> {
          _uiState.update { it.copy(tenantEnquiriesState = AsyncResourceState.Success(result.data)) }
        }
        is DwelloApiResult.Error -> {
          if (handleUnauthorizedIfNeeded(result)) return@launch
          _uiState.update { it.copy(tenantEnquiriesState = AsyncResourceState.Error(result.message)) }
        }
      }
    }
  }

  fun submitEnquiry(
    propertyId: String,
    message: String?,
    preferredInspectionDate: String?
  ) {
    if (!com.example.data.RoleNavigationRouter.canSubmitPropertyEnquiry(currentRole())) return
    if (_uiState.value.enquirySubmitting) return
    _uiState.update {
      it.copy(
        enquirySubmitting = true,
        enquirySubmitError = null,
        enquirySubmittedSuccess = false
      )
    }
    viewModelScope.launch {
      when (val result = repository.submitEnquiry(propertyId, message, preferredInspectionDate)) {
        is DwelloApiResult.Success -> {
          _uiState.update {
            it.copy(
              enquirySubmitting = false,
              enquirySubmitError = null,
              enquirySubmittedSuccess = true
            )
          }
          loadTenantEnquiries()
        }
        is DwelloApiResult.Error -> {
          if (handleUnauthorizedIfNeeded(result)) return@launch
          _uiState.update {
            it.copy(
              enquirySubmitting = false,
              enquirySubmitError = result.message,
              enquirySubmittedSuccess = false
            )
          }
        }
      }
    }
  }

  fun resetEnquirySubmissionState() {
    _uiState.update {
      it.copy(
        enquirySubmitting = false,
        enquirySubmitError = null,
        enquirySubmittedSuccess = false
      )
    }
  }

  fun loadLandlordData() {
    if (!com.example.data.RoleNavigationRouter.canAccessLandlordManagement(currentRole())) return
    _uiState.update {
      it.copy(
        landlordPropertiesState = AsyncResourceState.Loading,
        landlordEnquiriesState = AsyncResourceState.Loading
      )
    }
    viewModelScope.launch {
      val propsDeferred = async { repository.getLandlordProperties() }
      val enquiriesDeferred = async { repository.getLandlordEnquiries() }

      val propsResult = propsDeferred.await()
      val enquiriesResult = enquiriesDeferred.await()

      if ((propsResult is DwelloApiResult.Error && handleUnauthorizedIfNeeded(propsResult)) ||
        (enquiriesResult is DwelloApiResult.Error && handleUnauthorizedIfNeeded(enquiriesResult))
      ) {
        return@launch
      }

      _uiState.update { state ->
        state.copy(
          landlordPropertiesState = when (propsResult) {
            is DwelloApiResult.Success -> AsyncResourceState.Success(
              propsResult.data.map { mergeConfirmedImages(it) }
            )
            is DwelloApiResult.Error -> AsyncResourceState.Error(propsResult.message)
          },
          landlordEnquiriesState = when (enquiriesResult) {
            is DwelloApiResult.Success -> AsyncResourceState.Success(enquiriesResult.data)
            is DwelloApiResult.Error -> AsyncResourceState.Error(enquiriesResult.message)
          }
        )
      }
    }
  }

  fun loadLandlordEnquiries() {
    if (!com.example.data.RoleNavigationRouter.canAccessLandlordManagement(currentRole())) return
    _uiState.update { it.copy(landlordEnquiriesState = AsyncResourceState.Loading) }
    viewModelScope.launch {
      when (val result = repository.getLandlordEnquiries()) {
        is DwelloApiResult.Success -> {
          _uiState.update { it.copy(landlordEnquiriesState = AsyncResourceState.Success(result.data)) }
        }
        is DwelloApiResult.Error -> {
          if (handleUnauthorizedIfNeeded(result)) return@launch
          _uiState.update { it.copy(landlordEnquiriesState = AsyncResourceState.Error(result.message)) }
        }
      }
    }
  }

  fun createProperty(
    title: String,
    propertyTypeDisplay: String,
    bedrooms: Int,
    bathrooms: Int,
    rentAmount: Long,
    rentalPeriod: RentalPeriod,
    description: String,
    neighbourhood: String,
    fullAddress: String,
    landmark: String?,
    selectedPhotos: List<SelectedPhotoItem> = emptyList()
  ) {
    if (!com.example.data.RoleNavigationRouter.canAccessLandlordManagement(currentRole())) return
    if (_uiState.value.postPropertyLoading) return

    val existingPropertyId = _uiState.value.pendingUploadPropertyId
    val existingPendingPhotos = _uiState.value.pendingUploadPhotos
    val existingNextOrder = _uiState.value.pendingUploadNextDisplayOrder
    val existingConfirmed = _uiState.value.pendingUploadConfirmedImages

    _uiState.update {
      it.copy(
        postPropertyLoading = true,
        postPropertyUploadProgress = if (existingPropertyId != null) {
          "Retrying photo upload..."
        } else {
          "Saving property listing..."
        },
        postPropertyError = null,
        postPropertySuccessId = null
      )
    }

    viewModelScope.launch {
      val result = if (existingPropertyId != null) {
        val retryPhotos = existingPendingPhotos.ifEmpty { selectedPhotos }
        repository.uploadRemainingPropertyImages(
          propertyId = existingPropertyId,
          photos = retryPhotos,
          startDisplayOrder = existingNextOrder,
          alreadyUploaded = existingConfirmed,
          successMessage = "Property and photos submitted for Dwello Homes review.",
          onUploadProgress = { idx, total ->
            _uiState.update {
              it.copy(postPropertyUploadProgress = "Uploading photo $idx of $total...")
            }
          }
        )
      } else {
        repository.createPropertyWithImages(
          title = title,
          propertyTypeDisplay = propertyTypeDisplay,
          bedrooms = bedrooms,
          bathrooms = bathrooms,
          rentAmount = rentAmount,
          rentalPeriod = rentalPeriod,
          description = description,
          neighbourhood = neighbourhood,
          fullAddress = fullAddress,
          landmark = landmark,
          photos = selectedPhotos,
          onUploadProgress = { idx, total ->
            _uiState.update {
              it.copy(postPropertyUploadProgress = "Uploading photo $idx of $total...")
            }
          }
        )
      }

      when (result) {
        is PropertySubmissionResult.Success -> {
          if (result.uploadedImages.isNotEmpty()) {
            confirmedServerImagesCache[result.propertyId] = result.uploadedImages
          }
          _uiState.update {
            it.copy(
              postPropertyLoading = false,
              postPropertyUploadProgress = null,
              postPropertyError = null,
              postPropertySuccessId = result.propertyId.ifBlank { "submitted" },
              pendingUploadPropertyId = null,
              pendingUploadPhotos = emptyList(),
              pendingUploadNextDisplayOrder = 0,
              pendingUploadConfirmedImages = emptyList()
            )
          }
          loadLandlordData()
        }
        is PropertySubmissionResult.MetadataSavedUploadFailed -> {
          if (result.isUnauthorized) {
            handleUnauthorizedIfNeeded(
              DwelloApiResult.Error(result.message, httpCode = 401, isUnauthorized = true)
            )
            return@launch
          }
          if (result.uploadedImages.isNotEmpty()) {
            confirmedServerImagesCache[result.propertyId] = result.uploadedImages
          }
          _uiState.update {
            it.copy(
              postPropertyLoading = false,
              postPropertyUploadProgress = null,
              postPropertyError = "Property saved (ID: ${result.propertyId}), but photo upload failed: ${result.message}",
              postPropertySuccessId = null,
              pendingUploadPropertyId = result.propertyId,
              pendingUploadPhotos = result.remainingPhotos,
              pendingUploadNextDisplayOrder = result.nextDisplayOrder,
              pendingUploadConfirmedImages = result.uploadedImages
            )
          }
          loadLandlordData()
        }
        is PropertySubmissionResult.Failure -> {
          if (result.isUnauthorized) {
            handleUnauthorizedIfNeeded(
              DwelloApiResult.Error(result.message, httpCode = 401, isUnauthorized = true)
            )
            return@launch
          }
          _uiState.update {
            it.copy(
              postPropertyLoading = false,
              postPropertyUploadProgress = null,
              postPropertyError = result.message,
              postPropertySuccessId = null
            )
          }
        }
      }
    }
  }

  fun resetPostPropertyState() {
    _uiState.update {
      it.copy(
        postPropertyLoading = false,
        postPropertyUploadProgress = null,
        postPropertyError = null,
        postPropertySuccessId = null,
        pendingUploadPropertyId = null,
        pendingUploadPhotos = emptyList(),
        pendingUploadNextDisplayOrder = 0,
        pendingUploadConfirmedImages = emptyList()
      )
    }
  }

  fun updatePropertyListing(
    propertyId: String,
    title: String,
    rentAmount: Long,
    neighbourhood: String,
    description: String,
    fullAddress: String?,
    landmark: String?,
    existingImages: List<PropertyPhotoRecord> = emptyList(),
    imageIdsToDelete: List<String> = emptyList(),
    newPhotosToUpload: List<SelectedPhotoItem> = emptyList()
  ) {
    if (!com.example.data.RoleNavigationRouter.canAccessLandlordManagement(currentRole())) return
    if (_uiState.value.landlordActionLoading) return
    _uiState.update {
      it.copy(landlordActionLoading = true, landlordActionError = null, landlordActionMessage = null)
    }
    viewModelScope.launch {
      val updateResult = repository.updateProperty(
        propertyId = propertyId,
        title = title,
        rentAmount = rentAmount,
        neighbourhood = neighbourhood,
        description = description,
        fullAddress = fullAddress,
        landmark = landmark
      )

      when (updateResult) {
        is DwelloApiResult.Error -> {
          if (handleUnauthorizedIfNeeded(updateResult)) return@launch
          _uiState.update {
            it.copy(
              landlordActionLoading = false,
              landlordActionError = updateResult.message
            )
          }
          return@launch
        }
        is DwelloApiResult.Success -> Unit
      }

      val deleteIdSet = imageIdsToDelete.toSet()
      val remainingImages = existingImages
        .filter { record -> record.id == null || !deleteIdSet.contains(record.id) }
        .toMutableList()

      var photoOperationError: String? = null
      for (imageId in imageIdsToDelete) {
        when (val delRes = repository.deletePropertyImage(propertyId, imageId)) {
          is DwelloApiResult.Success -> Unit
          is DwelloApiResult.Error -> {
            if (handleUnauthorizedIfNeeded(delRes)) return@launch
            photoOperationError = "Listing details saved, but failed to remove an existing photo: ${delRes.message}"
            break
          }
        }
      }

      if (photoOperationError == null && newPhotosToUpload.isNotEmpty()) {
        val nextOrder = (remainingImages.maxOfOrNull { it.displayOrder } ?: -1) + 1
        when (
          val uploadRes = repository.uploadPropertyImages(
            propertyId = propertyId,
            photos = newPhotosToUpload,
            startDisplayOrder = nextOrder
          )
        ) {
          is DwelloApiResult.Success -> {
            remainingImages.addAll(uploadRes.data)
          }
          is DwelloApiResult.Error -> {
            if (handleUnauthorizedIfNeeded(uploadRes)) return@launch
            photoOperationError = "Listing details saved, but photo upload failed: ${uploadRes.message}"
          }
        }
      }

      if (remainingImages.isNotEmpty()) {
        confirmedServerImagesCache[propertyId] = remainingImages.sortedBy { it.displayOrder }
      } else if (imageIdsToDelete.isNotEmpty() && photoOperationError == null) {
        confirmedServerImagesCache.remove(propertyId)
      }

      _uiState.update {
        it.copy(
          landlordActionLoading = false,
          landlordActionMessage = if (photoOperationError == null) {
            updateResult.message ?: "Listing updated and sent for Dwello Homes approval."
          } else {
            null
          },
          landlordActionError = photoOperationError
        )
      }
      loadLandlordData()
    }
  }

  fun togglePropertyAvailability(property: PropertyItem) {
    if (!com.example.data.RoleNavigationRouter.canAccessLandlordManagement(currentRole())) return
    if (property.approvalStatus != com.example.data.PropertyApprovalStatus.APPROVED &&
      property.approvalStatus != com.example.data.PropertyApprovalStatus.UNAVAILABLE
    ) {
      return
    }
    if (_uiState.value.landlordActionLoading) return
    val makeAvailable = property.approvalStatus == com.example.data.PropertyApprovalStatus.UNAVAILABLE
    _uiState.update {
      it.copy(landlordActionLoading = true, landlordActionError = null, landlordActionMessage = null)
    }
    viewModelScope.launch {
      when (val result = repository.updatePropertyAvailability(property.id, makeAvailable)) {
        is DwelloApiResult.Success -> {
          val msg = if (makeAvailable) {
            "Property marked as Available."
          } else {
            "Property marked as Unavailable and hidden from tenant searches."
          }
          _uiState.update {
            it.copy(
              landlordActionLoading = false,
              landlordActionMessage = msg
            )
          }
          loadLandlordData()
        }
        is DwelloApiResult.Error -> {
          if (handleUnauthorizedIfNeeded(result)) return@launch
          _uiState.update {
            it.copy(
              landlordActionLoading = false,
              landlordActionError = result.message
            )
          }
        }
      }
    }
  }

  fun updateEnquiryStatus(enquiryId: String, newStatus: EnquiryStatus) {
    if (!com.example.data.RoleNavigationRouter.canAccessLandlordManagement(currentRole())) return
    if (_uiState.value.landlordActionLoading) return
    _uiState.update {
      it.copy(landlordActionLoading = true, landlordActionError = null, landlordActionMessage = null)
    }
    viewModelScope.launch {
      when (val result = repository.updateEnquiryStatus(enquiryId, newStatus)) {
        is DwelloApiResult.Success -> {
          _uiState.update {
            it.copy(
              landlordActionLoading = false,
              landlordActionMessage = "Enquiry marked as ${result.data.apiValue}."
            )
          }
          loadLandlordEnquiries()
        }
        is DwelloApiResult.Error -> {
          if (handleUnauthorizedIfNeeded(result)) return@launch
          _uiState.update {
            it.copy(
              landlordActionLoading = false,
              landlordActionError = result.message
            )
          }
        }
      }
    }
  }

  fun clearLandlordFeedback() {
    _uiState.update {
      it.copy(landlordActionMessage = null, landlordActionError = null)
    }
  }

  private fun handleUnauthorizedIfNeeded(error: DwelloApiResult.Error): Boolean {
    if (error.isUnauthorized) {
      repository.clearLocalSession()
      confirmedServerImagesCache.clear()
      _uiState.value = DwelloUiState(
        sessionStatus = SessionStatus.UNAUTHENTICATED,
        currentUser = null,
        authError = error.message
      )
      return true
    }
    return false
  }

  class Factory(private val repository: DwelloRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return DwelloViewModel(repository) as T
    }
  }
}
