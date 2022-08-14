package com.isaiahvonrundstedt.fokus.features.task

import org.junit.Test
import java.time.ZonedDateTime

class TaskUnitTest {

    @Test
    fun `should return true when a task has due date`() {
        val task = Task()
        task.dueDate = ZonedDateTime.now()

        assert(task.hasDueDate())
    }

    @Test
    fun `should return true when the task has due date in the future`() {
        val task = Task()
        task.dueDate = ZonedDateTime.now().plusDays(1)

        assert(task.isDueDateInFuture())
    }

    @Test
    fun `should return true when task has due date is today`() {
        val task = Task()
        task.dueDate = ZonedDateTime.now()

        assert(task.isDueToday())
    }
}