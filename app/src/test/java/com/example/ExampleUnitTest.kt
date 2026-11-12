package com.example

import com.example.data.PropertyApprovalStatus
import com.example.data.PropertyItem
import com.example.data.RentalPeriod
import com.example.data.SamplePropertyData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testNewPropertyDefaultsToPendingApproval() {
    val initialPendingCount = SamplePropertyData.dynamicProperties.count {
      it.approvalStatus == PropertyApprovalStatus.PENDING_APPROVAL
    }

    val newListing = PropertyItem(
      id = "test_prop_${System.currentTimeMillis()}",
      titleEn = "Test Flat in Uselu",
      titlePidgin = "Test Flat for Uselu",
      categoryEn = "2-bedroom apartment",
      categoryPidgin = "2-bedroom apartment",
      area = "Ugbowo",
      priceNaira = 350000L,
      priceFormatted = "₦350,000",
      rentalPeriod = RentalPeriod.YEARLY,
      bedrooms = 2,
      bathrooms = 1,
      landlordName = "Mr. Osasere",
      descriptionEn = "Test description",
      descriptionPidgin = "Test description",
      neighborhoodNote = "Uselu market axis",
      approvalStatus = PropertyApprovalStatus.APPROVED // Submitting with APPROVED flag
    )

    // Act
    SamplePropertyData.addProperty(newListing)

    // Assert: Business rule strictly enforces PENDING_APPROVAL regardless of input
    val addedItem = SamplePropertyData.dynamicProperties.first { it.id == newListing.id }
    assertEquals(PropertyApprovalStatus.PENDING_APPROVAL, addedItem.approvalStatus)
    assertNull(addedItem.rejectionReason)

    val updatedPendingCount = SamplePropertyData.dynamicProperties.count {
      it.approvalStatus == PropertyApprovalStatus.PENDING_APPROVAL
    }
    assertEquals(initialPendingCount + 1, updatedPendingCount)
  }

  @Test
  fun testOnlyApprovedPropertiesAppearInPublicSearch() {
    val publicListings = SamplePropertyData.getApprovedPublicProperties()

    // Assert: None of the public listings can be PENDING, REJECTED, or UNAVAILABLE
    assertTrue(publicListings.isNotEmpty())
    publicListings.forEach { property ->
      assertEquals(
        "Public search must only contain APPROVED properties: ${property.titleEn}",
        PropertyApprovalStatus.APPROVED,
        property.approvalStatus
      )
    }

    // Verify rejected and pending items are not in public search
    val pendingIds = SamplePropertyData.dynamicProperties
      .filter { it.approvalStatus == PropertyApprovalStatus.PENDING_APPROVAL }
      .map { it.id }
    val rejectedIds = SamplePropertyData.dynamicProperties
      .filter { it.approvalStatus == PropertyApprovalStatus.REJECTED }
      .map { it.id }

    val publicIds = publicListings.map { it.id }.toSet()
    pendingIds.forEach { assertFalse("Pending property $it must not be in public search", publicIds.contains(it)) }
    rejectedIds.forEach { assertFalse("Rejected property $it must not be in public search", publicIds.contains(it)) }
  }

  @Test
  fun testLandlordDashboardStatsCalculation() {
    val stats = SamplePropertyData.getLandlordStats(SamplePropertyData.CURRENT_LANDLORD_NAME)

    // Assert: Stats accurately tally all states
    assertTrue("Total properties should be >= 3", stats.totalProperties >= 3)
    assertTrue("Approved count should be >= 1", stats.approvedCount >= 1)
    assertTrue("Pending count should be >= 1", stats.pendingCount >= 1)
    assertTrue("Rejected count should be >= 1", stats.rejectedCount >= 1)
    assertTrue("Unavailable count should be >= 1", stats.unavailableCount >= 1)
    assertTrue("New enquiries count should be >= 1", stats.newEnquiriesCount >= 1)

    // Total must equal the sum of categories
    assertEquals(
      stats.totalProperties,
      stats.approvedCount + stats.pendingCount + stats.rejectedCount + stats.unavailableCount
    )
  }

  @Test
  fun testEditingRejectedPropertyResetsToPendingApproval() {
    val rejectedProperty = SamplePropertyData.dynamicProperties.firstOrNull {
      it.approvalStatus == PropertyApprovalStatus.REJECTED
    }
    assertNotNull("There should be a sample rejected property for testing", rejectedProperty)
    val propId = rejectedProperty!!.id
    assertNotNull(rejectedProperty.rejectionReason)

    // Act: Landlord edits the rejected listing
    val updateResult = SamplePropertyData.updatePropertyListing(
      propertyId = propId,
      newTitle = "Fixed Studio Self-Con Apartment",
      newPriceNaira = 230000L,
      newArea = "Sapele Road",
      newDescription = "Updated with clear compound exterior pictures",
      newRentalTerms = "1 Year rent in advance"
    )

    // Assert: Edit succeeded and status reset to PENDING_APPROVAL with cleared rejection reason
    assertTrue(updateResult)
    val updatedProp = SamplePropertyData.dynamicProperties.first { it.id == propId }
    assertEquals(PropertyApprovalStatus.PENDING_APPROVAL, updatedProp.approvalStatus)
    assertNull(updatedProp.rejectionReason)
    assertEquals("Fixed Studio Self-Con Apartment", updatedProp.titleEn)
  }

  @Test
  fun testToggleListingAvailability() {
    val approvedProp = SamplePropertyData.dynamicProperties.first {
      it.approvalStatus == PropertyApprovalStatus.APPROVED && it.landlordName == SamplePropertyData.CURRENT_LANDLORD_NAME
    }
    val propId = approvedProp.id

    // Act 1: Landlord marks it as unavailable
    val newStatus = SamplePropertyData.toggleListingAvailability(propId)
    assertEquals(PropertyApprovalStatus.UNAVAILABLE, newStatus)

    // Verify it is immediately excluded from public search
    val publicAfterMark = SamplePropertyData.getApprovedPublicProperties()
    assertFalse("Unavailable property must not appear in public search", publicAfterMark.any { it.id == propId })

    // Act 2: Landlord marks it as available again
    val restoredStatus = SamplePropertyData.toggleListingAvailability(propId)
    assertEquals(PropertyApprovalStatus.APPROVED, restoredStatus)

    val publicAfterRestore = SamplePropertyData.getApprovedPublicProperties()
    assertTrue("Restored property must appear in public search", publicAfterRestore.any { it.id == propId })
  }
}
