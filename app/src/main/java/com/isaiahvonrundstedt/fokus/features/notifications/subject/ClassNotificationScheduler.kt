package com.isaiahvonrundstedt.fokus.features.notifications.subject

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.isaiahvonrundstedt.fokus.database.repository.SubjectRepository
import com.isaiahvonrundstedt.fokus.features.shared.abstracts.BaseWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ClassNotificationScheduler @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val repository: SubjectRepository,
    private val workManager: WorkManager
) : BaseWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val subjectList = repository.fetch()

        subjectList.forEach { resource ->
            resource.schedules.forEach {
                it.subject = resource.subject.code

                val request = OneTimeWorkRequest.Builder(ClassNotificationWorker::class.java)
                    .setInputData(convertScheduleToData(it))
                workManager.enqueueUniqueWork(
                    it.scheduleID, ExistingWorkPolicy.REPLACE,
                    request.build()
                )
            }
        }
        return Result.success()
    }
}