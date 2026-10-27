package com.example.furnituregap.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.furnituregap.data.calculator.FurnitureGapCalculator
import com.example.furnituregap.data.model.AppSettings
import com.example.furnituregap.data.model.CalculationHistoryItem
import com.example.furnituregap.data.model.ClearanceDirection
import com.example.furnituregap.data.model.ClearanceResult
import com.example.furnituregap.data.model.ClearanceStatus
import com.example.furnituregap.data.model.Door
import com.example.furnituregap.data.model.FloorPlan
import com.example.furnituregap.data.model.FurnitureObject
import com.example.furnituregap.data.model.LengthUnit
import com.example.furnituregap.data.model.Rect
import com.example.furnituregap.data.model.WalkwayResult
import com.example.furnituregap.data.model.WallClearances
import com.example.furnituregap.data.model.Window
import com.example.furnituregap.data.repository.PlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.round

enum class CalculationMode(val title: String, val description: String) {
    FURNITURE_TO_WALL("Furniture → Wall", "Measure clearance between furniture and surrounding walls"),
    FURNITURE_TO_FURNITURE("Furniture → Furniture", "Edge-to-edge gap between two furniture pieces"),
    FURNITURE_TO_OBJECT("Furniture → Object", "Check door swing clearance and window sill alignments"),
    ROOM_LAYOUT("Room Layout", "Full room arrangement with multi-object clearances"),
    WALKWAY("Walkway", "Verify passage and path clearance requirements")
}

data class WallCalculatorState(
    val furnitureWidthMm: Double = 2000.0,  // 200 cm
    val furnitureDepthMm: Double = 900.0,   // 90 cm
    val furnitureHeightMm: Double = 850.0,  // 85 cm
    val roomWidthMm: Double = 4000.0,       // 400 cm
    val roomDepthMm: Double = 5000.0,       // 500 cm
    val leftOffsetMm: Double = 500.0,       // 50 cm
    val frontOffsetMm: Double = 600.0,      // 60 cm
    val requiredClearanceMm: Double = 600.0 // 60 cm
)

data class FurniturePairCalculatorState(
    val objectA: FurnitureObject = FurnitureObject(
        id = "calc_a",
        name = "Sofa",
        category = "Living Room",
        widthMm = 2000.0,
        depthMm = 900.0,
        heightMm = 850.0,
        xMm = 500.0,
        yMm = 1000.0,
        requiredClearanceMm = 600.0,
        colorHex = 0xFF2C3E50
    ),
    val objectB: FurnitureObject = FurnitureObject(
        id = "calc_b",
        name = "Coffee Table",
        category = "Living Room",
        widthMm = 1200.0,
        depthMm = 600.0,
        heightMm = 450.0,
        xMm = 500.0,
        yMm = 2300.0,
        requiredClearanceMm = 600.0,
        colorHex = 0xFF8D6E63
    )
)

data class WalkwayCalculatorState(
    val availableWidthMm: Double = 1200.0, // 120 cm
    val requiredWidthMm: Double = 900.0    // 90 cm standard walkway
)

data class DoorWindowCalculatorState(
    val doorWidthMm: Double = 900.0,
    val doorSwingDegrees: Double = 90.0,
    val doorHingeXmm: Double = 0.0,
    val doorHingeYmm: Double = 0.0,
    val furniturePassageWidthMm: Double = 800.0,
    val furnitureNearDoor: FurnitureObject = FurnitureObject(
        name = "Shoe Cabinet",
        widthMm = 800.0,
        depthMm = 350.0,
        xMm = 600.0,
        yMm = 600.0
    ),
    val windowWidthMm: Double = 1500.0,
    val windowSillHeightMm: Double = 900.0,
    val furnitureNearWindow: FurnitureObject = FurnitureObject(
        name = "Desk",
        widthMm = 1400.0,
        depthMm = 700.0,
        heightMm = 750.0,
        xMm = 500.0,
        yMm = 300.0
    )
)

class FurnitureGapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PlanRepository(application)

    val settings: StateFlow<AppSettings> = repository.settings
    val savedPlans: StateFlow<List<FloorPlan>> = repository.plans
    val history: StateFlow<List<CalculationHistoryItem>> = repository.history

    // Current calculation mode on Home screen
    private val _calculationMode = MutableStateFlow(CalculationMode.FURNITURE_TO_WALL)
    val calculationMode: StateFlow<CalculationMode> = _calculationMode.asStateFlow()

    // Mode-specific states for Home screen
    private val _wallCalcState = MutableStateFlow(WallCalculatorState())
    val wallCalcState: StateFlow<WallCalculatorState> = _wallCalcState.asStateFlow()

    private val _pairCalcState = MutableStateFlow(FurniturePairCalculatorState())
    val pairCalcState: StateFlow<FurniturePairCalculatorState> = _pairCalcState.asStateFlow()

    private val _walkwayCalcState = MutableStateFlow(WalkwayCalculatorState())
    val walkwayCalcState: StateFlow<WalkwayCalculatorState> = _walkwayCalcState.asStateFlow()

    private val _doorWindowCalcState = MutableStateFlow(DoorWindowCalculatorState())
    val doorWindowCalcState: StateFlow<DoorWindowCalculatorState> = _doorWindowCalcState.asStateFlow()

    // Active floor plan (used in Floor Plan & Clearance Analysis screens)
    private val _currentPlan = MutableStateFlow(
        repository.getLastPlan() ?: FloorPlan(name = "Living Room Plan")
    )
    val currentPlan: StateFlow<FloorPlan> = _currentPlan.asStateFlow()

    // Selected object on Floor Plan Canvas
    private val _selectedObjectId = MutableStateFlow<String?>(null)
    val selectedObjectId: StateFlow<String?> = _selectedObjectId.asStateFlow()

    // Selected pair on Floor Plan or Clearance Analysis
    private val _selectedPair = MutableStateFlow<Pair<String, String>?>(null)
    val selectedPair: StateFlow<Pair<String, String>?> = _selectedPair.asStateFlow()

    // Sorting option for Clearance Analysis
    enum class ClearanceSort(val label: String) {
        SMALLEST_GAP("Smallest Gap"),
        LARGEST_GAP("Largest Gap"),
        BELOW_TARGET("Below Target First"),
        OBJECT_NAME("Object Name")
    }

    private val _clearanceSort = MutableStateFlow(ClearanceSort.SMALLEST_GAP)
    val clearanceSort: StateFlow<ClearanceSort> = _clearanceSort.asStateFlow()

    fun setCalculationMode(mode: CalculationMode) {
        _calculationMode.value = mode
    }

    fun setClearanceSort(sort: ClearanceSort) {
        _clearanceSort.value = sort
    }

    // --- Wall Calculator methods ---

    fun updateWallCalculator(
        furnitureWidthMm: Double = _wallCalcState.value.furnitureWidthMm,
        furnitureDepthMm: Double = _wallCalcState.value.furnitureDepthMm,
        furnitureHeightMm: Double = _wallCalcState.value.furnitureHeightMm,
        roomWidthMm: Double = _wallCalcState.value.roomWidthMm,
        roomDepthMm: Double = _wallCalcState.value.roomDepthMm,
        leftOffsetMm: Double = _wallCalcState.value.leftOffsetMm,
        frontOffsetMm: Double = _wallCalcState.value.frontOffsetMm,
        requiredClearanceMm: Double = _wallCalcState.value.requiredClearanceMm
    ) {
        _wallCalcState.value = WallCalculatorState(
            furnitureWidthMm = furnitureWidthMm,
            furnitureDepthMm = furnitureDepthMm,
            furnitureHeightMm = furnitureHeightMm,
            roomWidthMm = roomWidthMm,
            roomDepthMm = roomDepthMm,
            leftOffsetMm = leftOffsetMm,
            frontOffsetMm = frontOffsetMm,
            requiredClearanceMm = requiredClearanceMm
        )
    }

    // --- Pair Calculator methods ---

    fun updatePairCalculator(
        objectA: FurnitureObject = _pairCalcState.value.objectA,
        objectB: FurnitureObject = _pairCalcState.value.objectB
    ) {
        _pairCalcState.value = FurniturePairCalculatorState(objectA = objectA, objectB = objectB)
    }

    fun rotateObjectA() {
        val curr = _pairCalcState.value.objectA
        val newRot = (curr.rotationDegrees + 90) % 360
        updatePairCalculator(objectA = curr.copy(rotationDegrees = newRot))
    }

    fun rotateObjectB() {
        val curr = _pairCalcState.value.objectB
        val newRot = (curr.rotationDegrees + 90) % 360
        updatePairCalculator(objectB = curr.copy(rotationDegrees = newRot))
    }

    // --- Walkway Calculator methods ---

    fun updateWalkwayCalculator(availableWidthMm: Double, requiredWidthMm: Double) {
        _walkwayCalcState.value = WalkwayCalculatorState(
            availableWidthMm = availableWidthMm,
            requiredWidthMm = requiredWidthMm
        )
    }

    // --- Door & Window Calculator methods ---

    fun updateDoorWindowCalculator(
        doorWidthMm: Double = _doorWindowCalcState.value.doorWidthMm,
        doorSwingDegrees: Double = _doorWindowCalcState.value.doorSwingDegrees,
        furniturePassageWidthMm: Double = _doorWindowCalcState.value.furniturePassageWidthMm,
        furnitureNearDoor: FurnitureObject = _doorWindowCalcState.value.furnitureNearDoor,
        windowWidthMm: Double = _doorWindowCalcState.value.windowWidthMm,
        windowSillHeightMm: Double = _doorWindowCalcState.value.windowSillHeightMm,
        furnitureNearWindow: FurnitureObject = _doorWindowCalcState.value.furnitureNearWindow
    ) {
        _doorWindowCalcState.value = DoorWindowCalculatorState(
            doorWidthMm = doorWidthMm,
            doorSwingDegrees = doorSwingDegrees,
            furniturePassageWidthMm = furniturePassageWidthMm,
            furnitureNearDoor = furnitureNearDoor,
            windowWidthMm = windowWidthMm,
            windowSillHeightMm = windowSillHeightMm,
            furnitureNearWindow = furnitureNearWindow
        )
    }

    // --- Plan & Canvas editing methods ---

    fun loadPlan(plan: FloorPlan) {
        _currentPlan.value = plan
        _selectedObjectId.value = plan.objects.firstOrNull()?.id
        _selectedPair.value = null
    }

    fun newEmptyPlan(name: String = "New Room Layout") {
        val newP = FloorPlan(name = name)
        _currentPlan.value = newP
        _selectedObjectId.value = null
        _selectedPair.value = null
    }

    fun saveCurrentPlan(name: String? = null) {
        val planToSave = if (name != null) _currentPlan.value.copy(name = name) else _currentPlan.value
        repository.savePlan(planToSave)
        _currentPlan.value = planToSave
    }

    fun deletePlan(id: String) {
        repository.deletePlan(id)
        if (_currentPlan.value.id == id) {
            val fallback = repository.getLastPlan() ?: FloorPlan(name = "New Plan")
            _currentPlan.value = fallback
        }
    }

    fun duplicatePlan(id: String) {
        repository.duplicatePlan(id)?.let {
            _currentPlan.value = it
        }
    }

    fun renamePlan(id: String, newName: String) {
        repository.renamePlan(id, newName)
        if (_currentPlan.value.id == id) {
            _currentPlan.value = _currentPlan.value.copy(name = newName)
        }
    }

    fun selectObject(id: String?) {
        _selectedObjectId.value = id
    }

    fun selectPair(pair: Pair<String, String>?) {
        _selectedPair.value = pair
    }

    fun updateRoomDimensions(widthMm: Double, depthMm: Double) {
        _currentPlan.value = _currentPlan.value.copy(
            roomWidthMm = maxOf(100.0, widthMm),
            roomDepthMm = maxOf(100.0, depthMm)
        )
    }

    fun addFurniture(obj: FurnitureObject) {
        val updated = _currentPlan.value.objects.toMutableList().apply { add(obj) }
        _currentPlan.value = _currentPlan.value.copy(objects = updated)
        _selectedObjectId.value = obj.id
    }

    fun removeFurniture(id: String) {
        val updated = _currentPlan.value.objects.filter { it.id != id }
        _currentPlan.value = _currentPlan.value.copy(objects = updated)
        if (_selectedObjectId.value == id) {
            _selectedObjectId.value = updated.firstOrNull()?.id
        }
    }

    fun updateFurniture(updatedObj: FurnitureObject) {
        val updated = _currentPlan.value.objects.map {
            if (it.id == updatedObj.id) updatedObj else it
        }
        _currentPlan.value = _currentPlan.value.copy(objects = updated)
    }

    fun moveFurniture(id: String, newXmm: Double, newYmm: Double, snap: Boolean = settings.value.snapToGrid) {
        val spacing = settings.value.defaultGridSpacingMm
        val finalX = if (snap && spacing > 0) (round(newXmm / spacing) * spacing) else newXmm
        val finalY = if (snap && spacing > 0) (round(newYmm / spacing) * spacing) else newYmm

        val updated = _currentPlan.value.objects.map {
            if (it.id == id) it.copy(xMm = maxOf(0.0, finalX), yMm = maxOf(0.0, finalY)) else it
        }
        _currentPlan.value = _currentPlan.value.copy(objects = updated)
    }

    fun rotateFurniture(id: String) {
        val updated = _currentPlan.value.objects.map {
            if (it.id == id) {
                val nextRot = (it.rotationDegrees + 90) % 360
                it.copy(rotationDegrees = nextRot)
            } else it
        }
        _currentPlan.value = _currentPlan.value.copy(objects = updated)
    }

    fun addDoor(door: Door) {
        val updated = _currentPlan.value.doors.toMutableList().apply { add(door) }
        _currentPlan.value = _currentPlan.value.copy(doors = updated)
    }

    fun removeDoor(id: String) {
        val updated = _currentPlan.value.doors.filter { it.id != id }
        _currentPlan.value = _currentPlan.value.copy(doors = updated)
    }

    fun addWindow(window: Window) {
        val updated = _currentPlan.value.windows.toMutableList().apply { add(window) }
        _currentPlan.value = _currentPlan.value.copy(windows = updated)
    }

    fun removeWindow(id: String) {
        val updated = _currentPlan.value.windows.filter { it.id != id }
        _currentPlan.value = _currentPlan.value.copy(windows = updated)
    }

    // --- History saving ---

    fun saveCurrentCalculationToHistory(title: String, subtitle: String, mode: String, clearanceMm: Double, reqMm: Double, status: ClearanceStatus, details: String) {
        val item = CalculationHistoryItem(
            title = title,
            subtitle = subtitle,
            calculationMode = mode,
            clearanceMm = clearanceMm,
            requiredClearanceMm = reqMm,
            status = status,
            details = details
        )
        repository.addHistoryItem(item)
    }

    fun deleteHistoryItem(id: String) {
        repository.deleteHistoryItem(id)
    }

    fun duplicateHistoryItem(id: String) {
        repository.duplicateHistoryItem(id)
    }

    // --- Settings ---

    fun updateSettings(newSettings: AppSettings) {
        repository.updateSettings(newSettings)
    }

    fun resetSettings() {
        repository.resetSettings()
    }
}
