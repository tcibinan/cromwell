package cromwell.backend.google.pipelines.v2alpha1

import wdl4s.parser.MemoryUnit
import wom.format.MemorySize

object MachineSpecifications {
  // https://cloud.google.com/compute/docs/instances/creating-instance-with-custom-machine-type
  // https://cloud.google.com/compute/docs/instances/creating-instance-with-custom-machine-type#specifications
  trait MachineSpecification {
    def getSupportedPlatforms:List[String] = List.empty
    def getMinMemoryPerCpu: MemorySize
    def getMaxMemoryPerCpu: MemorySize
    def getPrefix: String
  }

  private object N1MachineSpecification extends MachineSpecification {
    override def getMinMemoryPerCpu: MemorySize = MemorySize(0.9, MemoryUnit.GB)
    override def getMaxMemoryPerCpu: MemorySize = MemorySize(6.5, MemoryUnit.GB)
    override def getPrefix: String = ""
  }

  private object N2machineSpecification extends MachineSpecification {
    override def getSupportedPlatforms:List[String] = List("Intel Cascade Lake")
    override def getMinMemoryPerCpu: MemorySize = MemorySize(0.5, MemoryUnit.GB)
    override def getMaxMemoryPerCpu: MemorySize = MemorySize(8, MemoryUnit.GB)
    override def getPrefix: String = "n2-"
  }

  def getMachineTypeSpecifications(cpuPlatform: Option[String]): MachineSpecification = {
    if (cpuPlatform.isDefined
        && N2machineSpecification.getSupportedPlatforms.contains(cpuPlatform.get))
      N2machineSpecification
    else
      N1MachineSpecification
  }
}
