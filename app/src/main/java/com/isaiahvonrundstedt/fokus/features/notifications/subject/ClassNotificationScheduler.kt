package com.isaiahvonrundstedt.fokus.features.notifications.subject

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker

class ClassNotificationScheduler @WorkerInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: SubjectRepository
) : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val subjectList = repository.fetch()

        subjectList.forEach { resource ->
            resource.schedules.forEach {
                it.subject = resource.subject.code

                val request = OneTimeWorkRequest.Builder(ClassNotificationWorker::class.java)
                    .setInputData(convertScheduleToData(it))
                workManager.enqueueUniqueWork(it.scheduleID, ExistingWorkPolicy.REPLACE,
                    request.build())
            }
        }
        return Result.success()
    }
}