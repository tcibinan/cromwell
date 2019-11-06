package cromwell.backend.google.pipelines.common.api

import com.google.api.client.http.HttpRequest
import cromwell.backend.BackendJobDescriptor
import cromwell.backend.google.pipelines.common.PipelinesApiConfigurationAttributes.VirtualPrivateCloudConfiguration
import cromwell.backend.google.pipelines.common._
import cromwell.backend.google.pipelines.common.api.PipelinesApiRequestFactory.CreatePipelineParameters
import cromwell.backend.google.pipelines.common.io.PipelinesApiAttachedDisk
import cromwell.backend.standard.StandardAsyncJob
import cromwell.core.logging.JobLogger
import cromwell.core.path.Path
import wom.runtime.WomOutputRuntimeExtractor

/**
  * The PipelinesApiRequestFactory defines the HttpRequests needed to run jobs
  */
trait PipelinesApiRequestFactory {
  def runRequest(createPipelineParameters: CreatePipelineParameters, jobLogger: JobLogger): HttpRequest
  def getRequest(job: StandardAsyncJob): HttpRequest
  def cancelRequest(job: StandardAsyncJob): HttpRequest
}

object PipelinesApiRequestFactory {

  /**
    * Input parameters that are not strictly needed by the user's command but are Cromwell byproducts.
    */
  case class DetritusInputParameters(
                                      executionScriptInputParameter: PipelinesApiFileInput,
                                      monitoringScriptInputParameter: Option[PipelinesApiFileInput],
                                      initScriptInputParameter: Option[PipelinesApiFileInput]
                                    ) {
    def all: List[PipelinesApiFileInput] = List(executionScriptInputParameter) ++ monitoringScriptInputParameter ++ initScriptInputParameter
  }

  /**
    * Output parameters that are not produced by the user's command but are Cromwell byproducts.
    */
  case class DetritusOutputParameters(
                                       monitoringScriptOutputParameter: Option[PipelinesApiFileOutput],
                                       initScriptOutputParameter: Option[PipelinesApiFileOutput],
                                       rcFileOutputParameter: PipelinesApiFileOutput,
                                       memoryRetryRCFileOutputParameter: PipelinesApiFileOutput
                                    ) {
    def all: List[PipelinesApiFileOutput] = memoryRetryRCFileOutputParameter :: List(rcFileOutputParameter) ++ monitoringScriptOutputParameter ++ initScriptOutputParameter
  }

  /**
    * Bundle containing all input and output parameters to a PAPI job
    * Detrituses and actual inputs / outputs to the job are separated for more clarity and to leave open the possibility
    * to treat them differently.
    */
  case class InputOutputParameters(
                                    detritusInputParameters: DetritusInputParameters,
                                    jobInputParameters: List[PipelinesApiInput],
                                    jobOutputParameters: List[PipelinesApiOutput],
                                    detritusOutputParameters: DetritusOutputParameters,
                                    literalInputParameters: List[PipelinesApiLiteralInput]
                                  ) {
    lazy val fileInputParameters: List[PipelinesApiInput] = jobInputParameters ++ detritusInputParameters.all
    lazy val fileOutputParameters: List[PipelinesApiOutput] = detritusOutputParameters.all ++ jobOutputParameters
  }

  case class CreatePipelineDockerKeyAndToken(key: String, encryptedToken: String)
  
  case class CreatePipelineParameters(jobDescriptor: BackendJobDescriptor,
                                      runtimeAttributes: PipelinesApiRuntimeAttributes,
                                      dockerImage: String,
                                      cloudWorkflowRoot: Path,
                                      cloudCallRoot: Path,
                                      commandScriptContainerPath: Path,
                                      logGcsPath: Path,
                                      inputOutputParameters: InputOutputParameters,
                                      projectId: String,
                                      computeServiceAccount: String,
                                      googleLabels: Seq[GoogleLabel],
                                      preemptible: Boolean,
                                      jobShell: String,
                                      privateDockerKeyAndEncryptedToken: Option[CreatePipelineDockerKeyAndToken],
                                      womOutputRuntimeExtractor: Option[WomOutputRuntimeExtractor],
                                      adjustedSizeDisks: Seq[PipelinesApiAttachedDisk],
                                      virtualPrivateCloudConfiguration: Option[VirtualPrivateCloudConfiguration],
                                      retryWithMoreMemoryKeys: Option[List[String]]) {
    def literalInputs = inputOutputParameters.literalInputParameters
    def inputParameters = inputOutputParameters.fileInputParameters
    def outputParameters = inputOutputParameters.fileOutputParameters
    def allParameters = inputParameters ++ outputParameters
  }
}
