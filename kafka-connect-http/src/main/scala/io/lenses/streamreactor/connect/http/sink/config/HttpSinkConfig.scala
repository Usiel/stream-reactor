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
package io.lenses.streamreactor.connect.http.sink.config

import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Error
import io.lenses.streamreactor.common.config.SSLConfig
import io.lenses.streamreactor.connect.cloud.common.sink.commit.CommitPolicy
import io.lenses.streamreactor.connect.cloud.common.sink.commit.CommitPolicyCondition
import io.lenses.streamreactor.connect.cloud.common.sink.commit.Count
import io.lenses.streamreactor.connect.cloud.common.sink.commit.FileSize
import io.lenses.streamreactor.connect.cloud.common.sink.commit.Interval
import io.lenses.streamreactor.connect.http.sink.client.Authentication
import io.lenses.streamreactor.connect.http.sink.client.HttpMethod

import java.time.Clock
import java.time.Duration

object HttpSinkConfig {

  implicit val decoder: Decoder[HttpSinkConfig] = deriveDecoder
  implicit val encoder: Encoder[HttpSinkConfig] = deriveEncoder

  def fromJson(json: String): Either[Error, HttpSinkConfig] = decode[HttpSinkConfig](json)

}

case class BatchConfiguration(
  batchCount:   Option[Long],
  batchSize:    Option[Long],
  timeInterval: Option[Long],
) {
  def toCommitPolicy: CommitPolicy = {
    val conditions: Seq[CommitPolicyCondition] = Seq(
      batchCount.map(Count),
      batchSize.map(FileSize),
      timeInterval.map(inter => Interval(Duration.ofSeconds(inter), Clock.systemDefaultZone())),
    ).flatten

    CommitPolicy(conditions: _*)
  }
}

object BatchConfiguration {

  implicit val decoder: Decoder[BatchConfiguration] = deriveDecoder
  implicit val encoder: Encoder[BatchConfiguration] = deriveEncoder

}

case class HttpSinkConfig(
  authentication:   Option[Authentication], // basic, oauth2, proxy
  method:           HttpMethod,
  endpoint:         String, // tokenised
  content:          String, // tokenised
  headers:          Seq[(String, String)], // tokenised
  sslConfig:        Option[SSLConfig],
  batch:            Option[BatchConfiguration],
  errorThreshold:   Option[Int],
  uploadSyncPeriod: Option[Int],
) {
  def toJson: String = {
    val decoded: HttpSinkConfig = this
    decoded
      .asJson(HttpSinkConfig.encoder)
      .noSpaces
  }

}
