package com.reznor.emulation

import org.junit.Test
import org.junit.Assert.*
import com.reznor.emulation.model.EmulatorComponents
import com.reznor.emulation.model.EmulatorStatus

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class EmulatorComponentTest {
    @Test
    fun allComponents_areInitializedCorrectly() {
        val components = EmulatorComponents.ALL_COMPONENTS
        assertEquals(7, components.size)
        
        // Verify all components have required data
        components.forEach { component ->
            assertNotNull(component.id)
            assertNotNull(component.name)
            assertNotNull(component.description)
            assertNotNull(component.purpose)
            assertNotNull(component.estimatedSizeRange)
            assertTrue(component.id.isNotEmpty())
            assertTrue(component.name.isNotEmpty())
        }
    }
    
    @Test
    fun defaultStatus_isNotInstalled() {
        val components = EmulatorComponents.ALL_COMPONENTS
        components.forEach { component ->
            assertEquals(EmulatorStatus.NOT_INSTALLED, component.status)
        }
    }
    
    @Test
    fun specificEmulators_haveExpectedProperties() {
        val duckstation = EmulatorComponents.ALL_COMPONENTS.find { it.id == "duckstation" }
        assertNotNull(duckstation)
        assertEquals("DuckStation", duckstation!!.name)
        assertEquals("PS1 standalone", duckstation.purpose)
        
        val ppsspp = EmulatorComponents.ALL_COMPONENTS.find { it.id == "ppsspp" }
        assertNotNull(ppsspp)
        assertEquals("PPSSPP", ppsspp!!.name)
        assertEquals("PSP", ppsspp.purpose)
    }
}