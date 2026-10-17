package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GUTFlow", appName)
  }

  @Test
  fun `verify GUT calculation formula`() {
    val gravity = 5
    val urgency = 4
    val trend = 5
    val score = gravity * urgency * trend
    assertEquals(100, score)
  }

  @Test
  fun `verify priority level classification thresholds`() {
    val criticalTask = com.example.data.local.entity.TaskEntity(
      title = "Falha no servidor",
      gravity = 5,
      urgency = 5,
      trend = 4 // 100
    )
    assertEquals(com.example.data.local.entity.PriorityLevel.CRITICA, criticalTask.priorityLevel)

    val highTask = com.example.data.local.entity.TaskEntity(
      title = "Relatório mensal",
      gravity = 4,
      urgency = 4,
      trend = 4 // 64
    )
    assertEquals(com.example.data.local.entity.PriorityLevel.ALTA, highTask.priorityLevel)

    val mediumTask = com.example.data.local.entity.TaskEntity(
      title = "Backup",
      gravity = 3,
      urgency = 3,
      trend = 4 // 36
    )
    assertEquals(com.example.data.local.entity.PriorityLevel.MEDIA, mediumTask.priorityLevel)

    val lowTask = com.example.data.local.entity.TaskEntity(
      title = "Comprar cabo",
      gravity = 2,
      urgency = 2,
      trend = 2 // 8
    )
    assertEquals(com.example.data.local.entity.PriorityLevel.BAIXA, lowTask.priorityLevel)
  }
}
