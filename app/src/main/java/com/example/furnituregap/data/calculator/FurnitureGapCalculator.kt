package com.example.furnituregap.data.calculator

import com.example.furnituregap.data.model.BoundaryCheck
import com.example.furnituregap.data.model.ClearanceDirection
import com.example.furnituregap.data.model.ClearanceResult
import com.example.furnituregap.data.model.Door
import com.example.furnituregap.data.model.DoorSwingCheck
import com.example.furnituregap.data.model.FloorPlan
import com.example.furnituregap.data.model.FurnitureObject
import com.example.furnituregap.data.model.Rect
import com.example.furnituregap.data.model.WalkwayResult
import com.example.furnituregap.data.model.WallClearances
import com.example.furnituregap.data.model.Window
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object FurnitureGapCalculator {

    /**
     * Calculates the shortest edge-to-edge distance between two axis-aligned rectangles.
     * If they overlap, returns 0.0.
     */
    fun calculateRectangleDistance(a: Rect, b: Rect): Double {
        val dx = max(0.0, max(a.leftMm - b.rightMm, b.leftMm - a.rightMm))
        val dy = max(0.0, max(a.topMm - b.bottomMm, b.topMm - a.bottomMm))
        return sqrt((dx * dx) + (dy * dy))
    }

    /**
     * Checks if two rectangles overlap and computes the overlap width, depth, and area.
     */
    fun calculateOverlap(a: Rect, b: Rect): Triple<Double, Double, Double> {
        val overlapWidth = max(0.0, min(a.rightMm, b.rightMm) - max(a.leftMm, b.leftMm))
        val overlapDepth = max(0.0, min(a.bottomMm, b.bottomMm) - max(a.topMm, b.topMm))
        val overlapArea = if (overlapWidth > 0.0 && overlapDepth > 0.0) overlapWidth * overlapDepth else 0.0
        return Triple(overlapWidth, overlapDepth, overlapArea)
    }

    fun doRectanglesOverlap(a: Rect, b: Rect): Boolean {
        val (width, depth, _) = calculateOverlap(a, b)
        return width > 0.0 && depth > 0.0
    }

    /**
     * Determines the spatial relationship direction of B relative to A.
     */
    fun determineDirection(a: Rect, b: Rect): ClearanceDirection {
        val (overlapW, overlapD, _) = calculateOverlap(a, b)
        if (overlapW > 0.0 && overlapD > 0.0) {
            return ClearanceDirection.OVERLAP
        }

        val dx = max(0.0, max(a.leftMm - b.rightMm, b.leftMm - a.rightMm))
        val dy = max(0.0, max(a.topMm - b.bottomMm, b.topMm - a.bottomMm))

        // Check touching (distance == 0.0 without overlap)
        if (dx == 0.0 && dy == 0.0) {
            return ClearanceDirection.TOUCHING
        }

        // Pure horizontal separation
        if (dx > 0.0 && dy == 0.0) {
            return if (b.leftMm >= a.rightMm) ClearanceDirection.RIGHT else ClearanceDirection.LEFT
        }

        // Pure vertical separation
        if (dx == 0.0 && dy > 0.0) {
            return if (b.topMm >= a.bottomMm) ClearanceDirection.FRONT else ClearanceDirection.BACK
        }

        // Diagonal separation (both dx > 0 and dy > 0)
        return ClearanceDirection.DIAGONAL
    }

    /**
     * Calculates detailed clearance result between two furniture objects.
     */
    fun analyzePair(a: FurnitureObject, b: FurnitureObject): ClearanceResult {
        val rectA = a.toEffectiveRect()
        val rectB = b.toEffectiveRect()

        val (overlapWidth, overlapDepth, overlapArea) = calculateOverlap(rectA, rectB)
        val overlaps = overlapWidth > 0.0 && overlapDepth > 0.0
        val distance = if (overlaps) 0.0 else calculateRectangleDistance(rectA, rectB)
        val direction = determineDirection(rectA, rectB)

        // The required clearance is the maximum of the targets requested by either object
        val reqClearance = max(a.requiredClearanceMm, b.requiredClearanceMm)

        return ClearanceResult(
            objectAId = a.id,
            objectAName = a.name,
            objectBId = b.id,
            objectBName = b.name,
            distanceMm = distance,
            direction = direction,
            overlaps = overlaps,
            overlapWidthMm = overlapWidth,
            overlapDepthMm = overlapDepth,
            overlapAreaSquareMm = overlapArea,
            requiredClearanceMm = reqClearance,
            rectA = rectA,
            rectB = rectB
        )
    }

    /**
     * Calculates wall clearances for a rectangular room.
     */
    fun calculateWallClearances(
        roomWidthMm: Double,
        roomDepthMm: Double,
        furniture: Rect
    ): WallClearances {
        val left = furniture.leftMm
        val right = roomWidthMm - furniture.rightMm
        val top = furniture.topMm
        val bottom = roomDepthMm - furniture.bottomMm
        return WallClearances(
            leftMm = left,
            rightMm = right,
            topMm = top,
            bottomMm = bottom
        )
    }

    /**
     * Checks if a rectangle is within the room boundaries.
     */
    fun checkRoomBoundaries(
        roomWidthMm: Double,
        roomDepthMm: Double,
        furniture: Rect
    ): BoundaryCheck {
        val exceededLeft = if (furniture.leftMm < 0) -furniture.leftMm else 0.0
        val exceededTop = if (furniture.topMm < 0) -furniture.topMm else 0.0
        val exceededRight = if (furniture.rightMm > roomWidthMm) furniture.rightMm - roomWidthMm else 0.0
        val exceededBottom = if (furniture.bottomMm > roomDepthMm) furniture.bottomMm - roomDepthMm else 0.0

        val isOutside = exceededLeft > 0.0 || exceededTop > 0.0 || exceededRight > 0.0 || exceededBottom > 0.0
        return BoundaryCheck(
            isOutside = isOutside,
            exceededLeftMm = exceededLeft,
            exceededRightMm = exceededRight,
            exceededTopMm = exceededTop,
            exceededBottomMm = exceededBottom
        )
    }

    /**
     * Calculates pairwise clearances for all objects without self-pairs or reverse duplicates.
     */
    fun calculatePairwiseClearances(objects: List<FurnitureObject>): List<ClearanceResult> {
        val results = mutableListOf<ClearanceResult>()
        for (i in 0 until objects.size) {
            for (j in i + 1 until objects.size) {
                results.add(analyzePair(objects[i], objects[j]))
            }
        }
        return results
    }

    /**
     * Calculates the exact union area of a collection of rectangles using a sweep-line algorithm.
     * Prevents double-counting of overlapping furniture area.
     */
    fun calculateUnionArea(rectangles: List<Rect>): Double {
        val validRects = rectangles.filter { it.widthMm > 0 && it.depthMm > 0 }
        if (validRects.isEmpty()) return 0.0

        // Extract all unique X coordinates
        val xCoords = validRects.flatMap { listOf(it.leftMm, it.rightMm) }
            .distinct()
            .sorted()

        var totalArea = 0.0

        // Iterate over adjacent X intervals
        for (i in 0 until xCoords.size - 1) {
            val x1 = xCoords[i]
            val x2 = xCoords[i + 1]
            val width = x2 - x1
            if (width <= 0) continue

            // Find all rectangles that span this X interval
            val activeYIntervals = validRects
                .filter { it.leftMm <= x1 && it.rightMm >= x2 }
                .map { Pair(it.topMm, it.bottomMm) }
                .sortedBy { it.first }

            if (activeYIntervals.isEmpty()) continue

            // Merge overlapping Y intervals
            var currentYLength = 0.0
            var curStart = activeYIntervals[0].first
            var curEnd = activeYIntervals[0].second

            for (k in 1 until activeYIntervals.size) {
                val interval = activeYIntervals[k]
                if (interval.first <= curEnd) {
                    curEnd = max(curEnd, interval.second)
                } else {
                    currentYLength += (curEnd - curStart)
                    curStart = interval.first
                    curEnd = interval.second
                }
            }
            currentYLength += (curEnd - curStart)

            totalArea += (width * currentYLength)
        }

        return totalArea
    }

    /**
     * Calculates room occupancy percentage and free floor area.
     */
    fun calculateRoomOccupancy(
        roomWidthMm: Double,
        roomDepthMm: Double,
        objects: List<FurnitureObject>
    ): Pair<Double, Double> {
        val roomArea = roomWidthMm * roomDepthMm
        if (roomArea <= 0.0) return Pair(0.0, 0.0)

        val rects = objects.map { it.toEffectiveRect() }
        val unionArea = calculateUnionArea(rects)
        val occupancyPercent = (unionArea / roomArea) * 100.0
        val freeArea = max(0.0, roomArea - unionArea)
        return Pair(occupancyPercent, freeArea)
    }

    /**
     * Evaluates walkway clearance.
     */
    fun evaluateWalkway(availableWidthMm: Double, requiredWidthMm: Double): WalkwayResult {
        val remaining = availableWidthMm - requiredWidthMm
        return WalkwayResult(
            availableWidthMm = availableWidthMm,
            requiredWidthMm = requiredWidthMm,
            remainingWidthMm = remaining,
            fits = remaining >= 0.0
        )
    }

    /**
     * Checks if a furniture rectangle intersects the swing path of a door.
     */
    fun checkDoorSwingInterference(door: Door, furniture: FurnitureObject): DoorSwingCheck {
        val fRect = furniture.toEffectiveRect()
        val hx = door.hingeXmm
        val hy = door.hingeYmm
        val r = door.widthMm
        val rSq = r * r

        // Normalise angles in degrees [0, 360)
        val startAngle = ((door.rotation % 360.0) + 360.0) % 360.0
        val sweepAngle = door.swingDegrees

        // Helper to check if a point is within the door sector
        fun isPointInSector(px: Double, py: Double): Boolean {
            val dx = px - hx
            val dy = py - hy
            val distSq = (dx * dx) + (dy * dy)
            if (distSq > rSq) return false

            var angle = Math.toDegrees(atan2(dy, dx))
            if (angle < 0) angle += 360.0

            val relativeAngle = ((angle - startAngle) % 360.0 + 360.0) % 360.0
            return relativeAngle <= sweepAngle
        }

        // 1. Check if any corner of furniture is inside the sector
        val corners = listOf(
            Pair(fRect.leftMm, fRect.topMm),
            Pair(fRect.rightMm, fRect.topMm),
            Pair(fRect.leftMm, fRect.bottomMm),
            Pair(fRect.rightMm, fRect.bottomMm)
        )
        if (corners.any { isPointInSector(it.first, it.second) }) {
            return DoorSwingCheck(
                intersects = true,
                description = "Corner of ${furniture.name} enters door swing area"
            )
        }

        // 2. Check if the door's closed or open leaf intersects any furniture edge
        val startRad = Math.toRadians(startAngle)
        val endRad = Math.toRadians(startAngle + sweepAngle)
        val leafStart = Pair(hx + r * cos(startRad), hy + r * sin(startRad))
        val leafEnd = Pair(hx + r * cos(endRad), hy + r * sin(endRad))

        if (segmentIntersectsRect(hx, hy, leafStart.first, leafStart.second, fRect) ||
            segmentIntersectsRect(hx, hy, leafEnd.first, leafEnd.second, fRect)
        ) {
            return DoorSwingCheck(
                intersects = true,
                description = "Door leaf intersects ${furniture.name}"
            )
        }

        // 3. Check if the hinge is inside the furniture
        if (hx >= fRect.leftMm && hx <= fRect.rightMm && hy >= fRect.topMm && hy <= fRect.bottomMm) {
            return DoorSwingCheck(
                intersects = true,
                description = "Door hinge is inside ${furniture.name}"
            )
        }

        // 4. Check arc intersection with rectangle edges
        val closestX = max(fRect.leftMm, min(hx, fRect.rightMm))
        val closestY = max(fRect.topMm, min(hy, fRect.bottomMm))
        val cdx = closestX - hx
        val cdy = closestY - hy
        val closestDistSq = (cdx * cdx) + (cdy * cdy)

        if (closestDistSq <= rSq) {
            // Further verify if the closest point is within the angular sweep
            var angle = Math.toDegrees(atan2(cdy, cdx))
            if (angle < 0) angle += 360.0
            val relativeAngle = ((angle - startAngle) % 360.0 + 360.0) % 360.0
            if (relativeAngle <= sweepAngle) {
                return DoorSwingCheck(
                    intersects = true,
                    description = "Door swing arc sweeps through ${furniture.name}"
                )
            }
        }

        return DoorSwingCheck(
            intersects = false,
            description = "Door swing clear"
        )
    }

    private fun segmentIntersectsRect(
        x1: Double, y1: Double,
        x2: Double, y2: Double,
        rect: Rect
    ): Boolean {
        if (segmentsIntersect(x1, y1, x2, y2, rect.leftMm, rect.topMm, rect.rightMm, rect.topMm)) return true
        if (segmentsIntersect(x1, y1, x2, y2, rect.rightMm, rect.topMm, rect.rightMm, rect.bottomMm)) return true
        if (segmentsIntersect(x1, y1, x2, y2, rect.rightMm, rect.bottomMm, rect.leftMm, rect.bottomMm)) return true
        if (segmentsIntersect(x1, y1, x2, y2, rect.leftMm, rect.bottomMm, rect.leftMm, rect.topMm)) return true
        return false
    }

    private fun segmentsIntersect(
        ax1: Double, ay1: Double, ax2: Double, ay2: Double,
        bx1: Double, by1: Double, bx2: Double, by2: Double
    ): Boolean {
        fun ccw(px: Double, py: Double, qx: Double, qy: Double, rx: Double, ry: Double): Boolean {
            return (ry - py) * (qx - px) > (qy - py) * (rx - px)
        }
        return (ccw(ax1, ay1, bx1, by1, bx2, by2) != ccw(ax2, ay2, bx1, by1, bx2, by2)) &&
                (ccw(ax1, ay1, ax2, ay2, bx1, by1) != ccw(ax1, ay1, ax2, ay2, bx2, by2))
    }

    /**
     * Evaluates window clearance & sill height difference.
     */
    fun evaluateWindowClearance(window: Window, furniture: FurnitureObject): Pair<Double, Double> {
        val fRect = furniture.toEffectiveRect()
        // Window as an edge segment at top wall (or specified location)
        val wLeft = window.xMm
        val wRight = window.xMm + window.widthMm
        val wY = window.yMm

        val dx = max(0.0, max(fRect.leftMm - wRight, wLeft - fRect.rightMm))
        val dy = max(0.0, fRect.topMm - wY)
        val distance = sqrt((dx * dx) + (dy * dy))

        val heightDiff = furniture.heightMm - window.sillHeightMm
        return Pair(distance, heightDiff)
    }
}
