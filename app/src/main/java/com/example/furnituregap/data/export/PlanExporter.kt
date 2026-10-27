package com.example.furnituregap.data.export

import com.example.furnituregap.data.calculator.FurnitureGapCalculator
import com.example.furnituregap.data.model.ClearanceStatus
import com.example.furnituregap.data.model.FloorPlan
import com.example.furnituregap.data.model.LengthUnit
import org.json.JSONArray
import org.json.JSONObject

object PlanExporter {

    fun generateTxt(plan: FloorPlan, unit: LengthUnit = LengthUnit.CENTIMETERS, decimals: Int = 1): String {
        val pairs = FurnitureGapCalculator.calculatePairwiseClearances(plan.objects)
        val minPair = pairs.minByOrNull { it.distanceMm }
        val overallMinMm = minPair?.distanceMm ?: 0.0
        val targetClearanceMm = minPair?.requiredClearanceMm ?: 600.0

        val status = when {
            pairs.any { it.overlaps } -> "OVERLAPPING"
            minPair == null -> "NO_OBJECTS"
            overallMinMm < targetClearanceMm -> "BELOW TARGET"
            overallMinMm == 0.0 -> "TOUCHING"
            else -> "TARGET MET / ADEQUATE"
        }

        val sb = StringBuilder()
        sb.appendLine("FURNITUREGAP")
        sb.appendLine()
        sb.appendLine("PLAN:")
        sb.appendLine(plan.name)
        sb.appendLine()
        sb.appendLine("ROOM:")
        sb.appendLine("${unit.format(plan.roomWidthMm, decimals)} × ${unit.format(plan.roomDepthMm, decimals)}")
        sb.appendLine()
        sb.appendLine("OBJECTS:")
        sb.appendLine()
        plan.objects.forEach { obj ->
            sb.appendLine(obj.name)
            sb.appendLine("${unit.format(obj.widthMm, decimals)} × ${unit.format(obj.depthMm, decimals)}")
            sb.appendLine("Position:")
            sb.appendLine("${unit.format(obj.xMm, decimals)} × ${unit.format(obj.yMm, decimals)}")
            if (obj.rotationDegrees != 0) {
                sb.appendLine("Rotation: ${obj.rotationDegrees}°")
            }
            sb.appendLine()
        }

        sb.appendLine("CLEARANCE:")
        sb.appendLine()

        pairs.forEach { p ->
            sb.appendLine("${p.objectAName} ↔ ${p.objectBName}")
            if (p.overlaps) {
                sb.appendLine("OVERLAP (${unit.format(p.overlapWidthMm, decimals)} × ${unit.format(p.overlapDepthMm, decimals)})")
            } else {
                sb.appendLine(unit.format(p.distanceMm, decimals))
            }
            sb.appendLine()
        }

        // Wall clearances for each object
        plan.objects.forEach { obj ->
            val wc = FurnitureGapCalculator.calculateWallClearances(plan.roomWidthMm, plan.roomDepthMm, obj.toEffectiveRect())
            sb.appendLine("${obj.name} ↔ Left Wall")
            sb.appendLine(unit.format(wc.leftMm, decimals))
            sb.appendLine()
            sb.appendLine("${obj.name} ↔ Right Wall")
            sb.appendLine(unit.format(wc.rightMm, decimals))
            sb.appendLine()
        }

        sb.appendLine("MINIMUM CLEARANCE:")
        sb.appendLine(unit.format(overallMinMm, decimals))
        sb.appendLine()
        sb.appendLine("REQUIRED:")
        sb.appendLine(unit.format(targetClearanceMm, decimals))
        sb.appendLine()
        sb.appendLine("STATUS:")
        sb.appendLine(status)

        return sb.toString()
    }

    fun generateCsv(plan: FloorPlan, unit: LengthUnit = LengthUnit.CENTIMETERS, decimals: Int = 1): String {
        val pairs = FurnitureGapCalculator.calculatePairwiseClearances(plan.objects)
        val sb = StringBuilder()
        sb.appendLine("Object A,Object B,Clearance,Direction,Required,Margin,Status")

        fun escapeCsv(s: String): String {
            return if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
                "\"${s.replace("\"", "\"\"")}\""
            } else s
        }

        pairs.forEach { p ->
            val clearanceVal = unit.format(p.distanceMm, decimals, includeSymbol = false)
            val reqVal = unit.format(p.requiredClearanceMm, decimals, includeSymbol = false)
            val marginVal = unit.format(p.marginMm, decimals, includeSymbol = false)
            sb.appendLine("${escapeCsv(p.objectAName)},${escapeCsv(p.objectBName)},$clearanceVal,${p.direction.label},$reqVal,$marginVal,${p.status.label}")
        }

        // Add wall clearances to CSV
        plan.objects.forEach { obj ->
            val wc = FurnitureGapCalculator.calculateWallClearances(plan.roomWidthMm, plan.roomDepthMm, obj.toEffectiveRect())
            val reqVal = unit.format(obj.requiredClearanceMm, decimals, includeSymbol = false)

            val leftMargin = unit.format(wc.leftMm - obj.requiredClearanceMm, decimals, includeSymbol = false)
            val leftStatus = if (wc.leftMm >= obj.requiredClearanceMm) "Adequate" else "Below Target"
            sb.appendLine("${escapeCsv(obj.name)},Left Wall,${unit.format(wc.leftMm, decimals, false)},Left,$reqVal,$leftMargin,$leftStatus")

            val rightMargin = unit.format(wc.rightMm - obj.requiredClearanceMm, decimals, includeSymbol = false)
            val rightStatus = if (wc.rightMm >= obj.requiredClearanceMm) "Adequate" else "Below Target"
            sb.appendLine("${escapeCsv(obj.name)},Right Wall,${unit.format(wc.rightMm, decimals, false)},Right,$reqVal,$rightMargin,$rightStatus")
        }

        return sb.toString()
    }

    fun generateJson(plan: FloorPlan): String {
        val json = JSONObject()
        json.put("planName", plan.name)

        val roomJson = JSONObject()
        roomJson.put("widthCm", plan.roomWidthMm / 10.0)
        roomJson.put("depthCm", plan.roomDepthMm / 10.0)
        json.put("room", roomJson)

        val objectsArray = JSONArray()
        plan.objects.forEach { obj ->
            val objJson = JSONObject()
            objJson.put("name", obj.name)
            objJson.put("category", obj.category)
            objJson.put("widthCm", obj.widthMm / 10.0)
            objJson.put("depthCm", obj.depthMm / 10.0)
            objJson.put("heightCm", obj.heightMm / 10.0)
            objJson.put("xCm", obj.xMm / 10.0)
            objJson.put("yCm", obj.yMm / 10.0)
            objJson.put("rotation", obj.rotationDegrees)
            objJson.put("requiredClearanceCm", obj.requiredClearanceMm / 10.0)
            objJson.put("notes", obj.notes)
            objectsArray.put(objJson)
        }
        json.put("objects", objectsArray)

        val pairs = FurnitureGapCalculator.calculatePairwiseClearances(plan.objects)
        val minPair = pairs.minByOrNull { it.distanceMm }
        val minClearanceCm = (minPair?.distanceMm ?: 0.0) / 10.0
        val reqClearanceCm = (minPair?.requiredClearanceMm ?: 600.0) / 10.0

        val analysisJson = JSONObject()
        analysisJson.put("minimumClearanceCm", minClearanceCm)
        analysisJson.put("requiredClearanceCm", reqClearanceCm)
        analysisJson.put("status", minPair?.status?.name ?: "NO_OBJECTS")
        json.put("clearanceAnalysis", analysisJson)

        return json.toString(2)
    }

    fun generateShareText(plan: FloorPlan, unit: LengthUnit = LengthUnit.CENTIMETERS, decimals: Int = 1): String {
        val pairs = FurnitureGapCalculator.calculatePairwiseClearances(plan.objects)
        val minPair = pairs.minByOrNull { it.distanceMm }
        val minGap = minPair?.distanceMm ?: 0.0
        val target = minPair?.requiredClearanceMm ?: 600.0
        val diff = target - minGap

        val statusStr = when {
            pairs.any { it.overlaps } -> "BLOCKED / OVERLAPPING"
            minPair == null -> "NO OBJECTS"
            minGap >= target -> "ADEQUATE"
            else -> "BELOW TARGET"
        }

        val betweenStr = if (minPair != null) "${minPair.objectAName} ↔ ${minPair.objectBName}" else "N/A"

        return buildString {
            appendLine("FurnitureGap")
            appendLine()
            appendLine(plan.name)
            appendLine()
            appendLine("Minimum Clearance:")
            appendLine(unit.format(minGap, decimals))
            appendLine()
            appendLine("Required:")
            appendLine(unit.format(target, decimals))
            appendLine()
            if (minGap < target) {
                appendLine("Short By:")
                appendLine(unit.format(diff, decimals))
            } else {
                appendLine("Extra:")
                appendLine(unit.format(-diff, decimals))
            }
            appendLine()
            appendLine("Between:")
            appendLine(betweenStr)
            appendLine()
            appendLine("Status:")
            appendLine(statusStr)
        }
    }
}
