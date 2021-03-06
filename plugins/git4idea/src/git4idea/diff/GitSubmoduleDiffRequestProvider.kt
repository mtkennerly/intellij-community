// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package git4idea.diff

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffRequestFactoryImpl
import com.intellij.diff.DiffRequestFactoryImpl.DIFF_TITLE_RENAME_SEPARATOR
import com.intellij.diff.chains.DiffRequestProducerException
import com.intellij.diff.requests.DiffRequest
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.actions.diff.ChangeDiffRequestProducer
import com.intellij.openapi.vcs.changes.actions.diff.ChangeDiffRequestProducer.*
import com.intellij.openapi.vcs.changes.actions.diff.ChangeDiffRequestProvider
import com.intellij.util.ThreeState

class GitSubmoduleDiffRequestProvider : ChangeDiffRequestProvider {
  override fun isEquals(change1: Change, change2: Change): ThreeState {
    return ThreeState.UNSURE
  }

  override fun canCreate(project: Project?, change: Change): Boolean {
    val beforeRevision = change.beforeRevision
    val afterRevision = change.afterRevision
    return (beforeRevision == null || beforeRevision is GitSubmoduleContentRevision) &&
           (afterRevision == null || afterRevision is GitSubmoduleContentRevision)
  }

  @Throws(ProcessCanceledException::class, DiffRequestProducerException::class)
  override fun process(presentable: ChangeDiffRequestProducer,
                       context: UserDataHolder,
                       indicator: ProgressIndicator): DiffRequest {
    val change = presentable.change
    val beforeRevision = change.beforeRevision
    val afterRevision = change.afterRevision
    val factory = DiffContentFactory.getInstance()
    val beforeContent = beforeRevision?.content?.let { factory.create(it) } ?: factory.createEmpty()
    val afterContent = afterRevision?.content?.let { factory.create(it) } ?: factory.createEmpty()
    return SimpleDiffRequest(DiffRequestFactoryImpl.getTitle(beforeRevision?.file, afterRevision?.file, DIFF_TITLE_RENAME_SEPARATOR) +  " (Submodule)",
                             beforeContent,
                             afterContent,
                             getRevisionTitle(beforeRevision, BASE_VERSION),
                             getRevisionTitle(afterRevision, YOUR_VERSION))
  }
}
