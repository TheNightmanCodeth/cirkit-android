package me.thenightmancodeth.cirkit2

import me.thenightmancodeth.cirkit2.network.Cirkit
import org.junit.Before
import org.junit.Test

/**
 * Created by joe on 5/17/17.
 */
class CirkitTest {
    private lateinit var cirkit: Cirkit

    @Before
    fun setup() {
        cirkit = Cirkit()
    }

    @Test
    fun testFileUL() {
        var file: File = File("res/test.png")
        cirkit.sendImagePush(file=file)
    }
}
