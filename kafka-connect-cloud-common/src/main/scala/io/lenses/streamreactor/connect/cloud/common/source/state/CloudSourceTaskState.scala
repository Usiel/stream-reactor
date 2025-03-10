/*
 * Copyright 2017-2024 Lenses.io Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.lenses.streamreactor.connect.cloud.common.source.state

import cats.effect.IO
import cats.implicits._
import io.lenses.streamreactor.connect.cloud.common.source.reader.ReaderManager
import org.apache.kafka.connect.source.SourceRecord

class CloudSourceTaskState(
  latestReaderManagers: IO[Seq[ReaderManager]],
) {

  def close(): IO[Unit] =
    latestReaderManagers.flatMap(_.traverse(_.close())).attempt.void

  def poll(): IO[Seq[SourceRecord]] =
    for {
      readers      <- latestReaderManagers
      pollResults  <- readers.map(_.poll()).traverse(identity)
      sourceRecords = pollResults.flatten
    } yield sourceRecords
}
