package com.example.furnituregap

import com.example.furnituregap.data.calculator.FurnitureGapCalculator
import com.example.furnituregap.data.model.ClearanceDirection
import com.example.furnituregap.data.model.ClearanceResult
import com.example.furnituregap.data.model.ClearanceStatus
import com.example.furnituregap.data.model.Door
import com.example.furnituregap.data.model.FurnitureObject
import com.example.furnituregap.data.model.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class FurnitureGapCalculatorTest {

    @Test
    fun testWallClearances() {
        // Room: 500 x 400 cm (5000 x 4000 mm)
        // Furniture: 200 x 90 cm (2000 x 900 mm)
        // Position: x = 50 cm (500 mm), y = 60 cm (600 mm)
        val roomWidthMm = 5000.0
        val roomDepthMm = 4000.0
        val furniture = Rect(leftMm = 500.0, topMm = 600.0, widthMm = 2000.0, depthMm = 900.0)

        val clearances = FurnitureGapCalculator.calculateWallClearances(roomWidthMm, roomDepthMm, furniture)

        // Expected: Left 50 cm (500 mm), Right 250 cm (2500 mm), Top 60 cm (600 mm), Bottom 250 cm (2500 mm)
        assertEquals(500.0, clearances.leftMm, 0.001)
        assertEquals(2500.0, clearances.rightMm, 0.001)
        assertEquals(600.0, clearances.topMm, 0.001)
        assertEquals(2500.0, clearances.bottomMm, 0.001)

        // Minimum clearance: 50 cm (500 mm)
        assertEquals(500.0, clearances.minimumClearanceMm, 0.001)
    }

    @Test
    fun testFurnitureToFurnitureClearance() {
        // Object A: left = 50, top = 100, width = 200, depth = 90 (in cm -> *10 mm)
        val a = Rect(leftMm = 500.0, topMm = 1000.0, widthMm = 2000.0, depthMm = 900.0)
        // Object B: left = 50, top = 230, width = 120, depth = 60 (in cm -> *10 mm)
        val b = Rect(leftMm = 500.0, topMm = 2300.0, widthMm = 1200.0, depthMm = 600.0)

        val distance = FurnitureGapCalculator.calculateRectangleDistance(a, b)
        val direction = FurnitureGapCalculator.determineDirection(a, b)

        // Expected vertical gap: 230 - (100 + 90) = 40 cm (400 mm)
        assertEquals(400.0, distance, 0.001)
        assertEquals(ClearanceDirection.FRONT, direction)
    }

    @Test
    fun testOverlap() {
        // A: 0, 0, 100 x 100 cm (1000 x 1000 mm)
        val a = Rect(leftMm = 0.0, topMm = 0.0, widthMm = 1000.0, depthMm = 1000.0)
        // B: 50, 50, 100 x 100 cm (500, 500, 1000 x 1000 mm)
        val b = Rect(leftMm = 500.0, topMm = 500.0, widthMm = 1000.0, depthMm = 1000.0)

        val (overlapWidth, overlapDepth, overlapArea) = FurnitureGapCalculator.calculateOverlap(a, b)
        val overlaps = FurnitureGapCalculator.doRectanglesOverlap(a, b)
        val direction = FurnitureGapCalculator.determineDirection(a, b)

        // Expected: overlap width 500 mm (50 cm), depth 500 mm (50 cm), area 250,000 mm² (2500 cm²)
        assertEquals(500.0, overlapWidth, 0.001)
        assertEquals(500.0, overlapDepth, 0.001)
        assertEquals(250000.0, overlapArea, 0.001)
        assertTrue(overlaps)
        assertEquals(ClearanceDirection.OVERLAP, direction)

        val res = ClearanceResult(
            objectAId = "A",
            objectBId = "B",
            distanceMm = 0.0,
            direction = direction,
            overlaps = overlaps,
            overlapWidthMm = overlapWidth,
            overlapDepthMm = overlapDepth,
            overlapAreaSquareMm = overlapArea,
            requiredClearanceMm = 600.0
        )
        assertEquals(ClearanceStatus.OVERLAPPING, res.status)
    }

    @Test
    fun testTouching() {
        // A: 0, 0, 100 x 100 cm (1000 x 1000 mm)
        val a = Rect(leftMm = 0.0, topMm = 0.0, widthMm = 1000.0, depthMm = 1000.0)
        // B: 100, 0, 100 x 100 cm (1000, 0, 1000 x 1000 mm)
        val b = Rect(leftMm = 1000.0, topMm = 0.0, widthMm = 1000.0, depthMm = 1000.0)

        val distance = FurnitureGapCalculator.calculateRectangleDistance(a, b)
        val overlaps = FurnitureGapCalculator.doRectanglesOverlap(a, b)
        val direction = FurnitureGapCalculator.determineDirection(a, b)

        assertEquals(0.0, distance, 0.001)
        assertFalse(overlaps)
        assertEquals(ClearanceDirection.TOUCHING, direction)

        val res = ClearanceResult(
            objectAId = "A",
            objectBId = "B",
            distanceMm = distance,
            direction = direction,
            overlaps = overlaps,
            requiredClearanceMm = 600.0
        )
        assertEquals(ClearanceStatus.TOUCHING, res.status)
    }

    @Test
    fun testDiagonal() {
        // A: 0, 0, 100 x 100 cm (1000 x 1000 mm)
        val a = Rect(leftMm = 0.0, topMm = 0.0, widthMm = 1000.0, depthMm = 1000.0)
        // B: 150, 150, 100 x 100 cm (1500, 1500, 1000 x 1000 mm)
        val b = Rect(leftMm = 1500.0, topMm = 1500.0, widthMm = 1000.0, depthMm = 1000.0)

        val dx = 1500.0 - 1000.0 // 500 mm
        val dy = 1500.0 - 1000.0 // 500 mm
        val expectedDist = sqrt((500.0 * 500.0) + (500.0 * 500.0))

        val distance = FurnitureGapCalculator.calculateRectangleDistance(a, b)
        val direction = FurnitureGapCalculator.determineDirection(a, b)

        assertEquals(expectedDist, distance, 0.001)
        assertEquals(ClearanceDirection.DIAGONAL, direction)
    }

    @Test
    fun testRequiredClearanceStatus() {
        // Actual 45 cm (450 mm), Required 60 cm (600 mm) -> Margin -150 mm, BELOW_TARGET
        val resBelow = ClearanceResult(
            objectAId = "A",
            objectBId = "B",
            distanceMm = 450.0,
            direction = ClearanceDirection.FRONT,
            overlaps = false,
            requiredClearanceMm = 600.0
        )
        assertEquals(-150.0, resBelow.marginMm, 0.001)
        assertEquals(ClearanceStatus.BELOW_TARGET, resBelow.status)

        // Actual 75 cm (750 mm), Required 60 cm (600 mm) -> Margin +150 mm, ADEQUATE
        val resAdequate = ClearanceResult(
            objectAId = "A",
            objectBId = "B",
            distanceMm = 750.0,
            direction = ClearanceDirection.FRONT,
            overlaps = false,
            requiredClearanceMm = 600.0
        )
        assertEquals(150.0, resAdequate.marginMm, 0.001)
        assertEquals(ClearanceStatus.ADEQUATE, resAdequate.status)
    }

    @Test
    fun testRoomBoundaryCheck() {
        // Room 500 x 400 cm (5000 x 4000 mm)
        // Furniture: 200 x 90 cm (2000 x 900 mm), x = 350 cm (3500 mm)
        val roomW = 5000.0
        val roomD = 4000.0
        val f = Rect(leftMm = 3500.0, topMm = 1000.0, widthMm = 2000.0, depthMm = 900.0)

        // Right edge is 3500 + 2000 = 5500 mm. Room width is 5000 mm. Exceeds by 500 mm (50 cm).
        val check = FurnitureGapCalculator.checkRoomBoundaries(roomW, roomD, f)
        assertTrue(check.isOutside)
        assertEquals(500.0, check.exceededRightMm, 0.001)
        assertEquals(0.0, check.exceededLeftMm, 0.001)
        assertEquals(0.0, check.exceededTopMm, 0.001)
        assertEquals(0.0, check.exceededBottomMm, 0.001)
    }

    @Test
    fun testRotationDimensions() {
        // 200 x 80 cm (2000 x 800 mm) rotated 90° -> 800 x 2000 mm
        val f = FurnitureObject(
            name = "Table",
            widthMm = 2000.0,
            depthMm = 800.0,
            rotationDegrees = 90
        )
        assertEquals(800.0, f.effectiveWidthMm, 0.001)
        assertEquals(2000.0, f.effectiveDepthMm, 0.001)
    }

    @Test
    fun testRoomOccupancy() {
        // Room 500 x 400 cm = 200,000 cm² (20,000,000 mm²)
        // Furniture 200 x 100 cm = 20,000 cm² (2,000,000 mm²) -> 10%
        val obj = FurnitureObject(
            name = "Desk",
            widthMm = 2000.0,
            depthMm = 1000.0,
            xMm = 100.0,
            yMm = 100.0
        )
        val (occupancyPercent, freeArea) = FurnitureGapCalculator.calculateRoomOccupancy(5000.0, 4000.0, listOf(obj))
        assertEquals(10.0, occupancyPercent, 0.001)
        assertEquals(18000000.0, freeArea, 0.001)
    }

    @Test
    fun testRectangleUnionAreaOverlapping() {
        // Two overlapping 1000 x 1000 mm rectangles with 500 x 500 mm overlap
        // Total area should be 1,000,000 + 1,000,000 - 250,000 = 1,750,000 mm²
        val r1 = Rect(0.0, 0.0, 1000.0, 1000.0)
        val r2 = Rect(500.0, 500.0, 1000.0, 1000.0)
        val unionArea = FurnitureGapCalculator.calculateUnionArea(listOf(r1, r2))
        assertEquals(1750000.0, unionArea, 0.001)
    }

    @Test
    fun testMultipleObjectPairwise() {
        val sofa = FurnitureObject(id = "1", name = "Sofa", widthMm = 2000.0, depthMm = 900.0, xMm = 500.0, yMm = 500.0)
        val table = FurnitureObject(id = "2", name = "Coffee Table", widthMm = 1200.0, depthMm = 600.0, xMm = 500.0, yMm = 1800.0)
        val tvStand = FurnitureObject(id = "3", name = "TV Stand", widthMm = 1800.0, depthMm = 450.0, xMm = 500.0, yMm = 3000.0)
        val armchair = FurnitureObject(id = "4", name = "Armchair", widthMm = 900.0, depthMm = 850.0, xMm = 3000.0, yMm = 500.0)

        val pairs = FurnitureGapCalculator.calculatePairwiseClearances(listOf(sofa, table, tvStand, armchair))
        // 4 objects -> 4 * 3 / 2 = 6 pairs
        assertEquals(6, pairs.size)

        // Verify no pair has same A and B
        pairs.forEach {
            assertTrue(it.objectAId != it.objectBId)
        }

        // Sofa to Table gap: y: 1800 - (500 + 900) = 400 mm
        val sofaTable = pairs.first { (it.objectAId == "1" && it.objectBId == "2") || (it.objectAId == "2" && it.objectBId == "1") }
        assertEquals(400.0, sofaTable.distanceMm, 0.001)
    }

    @Test
    fun testDoorSwingInterference() {
        val door = Door(
            hingeXmm = 0.0,
            hingeYmm = 0.0,
            widthMm = 900.0,
            swingDegrees = 90.0,
            rotation = 0.0 // swings in quadrant +X to +Y
        )
        // Furniture inside the swing sector
        val blockedObj = FurnitureObject(
            name = "Shoe Rack",
            widthMm = 400.0,
            depthMm = 400.0,
            xMm = 200.0,
            yMm = 200.0
        )
        val checkBlocked = FurnitureGapCalculator.checkDoorSwingInterference(door, blockedObj)
        assertTrue(checkBlocked.intersects)

        // Furniture safely outside the swing sector
        val clearObj = FurnitureObject(
            name = "Coat Stand",
            widthMm = 400.0,
            depthMm = 400.0,
            xMm = 1200.0,
            yMm = 1200.0
        )
        val checkClear = FurnitureGapCalculator.checkDoorSwingInterference(door, clearObj)
        assertFalse(checkClear.intersects)
    }
}
