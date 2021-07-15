
        set ArNu 10
        set d1 100
        set d2 100
        set d3 0
        set trRng 25
        set PrSns 10

 cd [mkdir -q drcl.comp.Component /MHTrAgg]
# TOTAL number of nodes (sensor nodes + target nodes)
    set node_num 52
# Number of TARGET nodes ONLY
    set target_node_num 1

# Hence, number of SENSORS = node_num - target_node_num
    set sink_id 0
# create the sensor channel
mkdir drcl.inet.sensorsim.SensorChannel chan

# Capacity of the sensor channel is total number of nodes (sensors + targets)
# make simulation for $node_num nodes
! chan setCapacity $node_num

# create the propagation model
mkdir drcl.inet.sensorsim.SeismicProp seismic_Prop
! seismic_Prop setD0 0.2

# create the sensor node position tracker
mkdir drcl.inet.sensorsim.SensorNodePositionTracker nodetracker
					# maxX  minX maxY minY
! nodetracker setGrid 100.0 0.0 100.0 0.0

# connect the sensor channel to the sensor node position tracker
connect chan/.tracker@ -and nodetracker/.channel@
# create the wireless channel
    mkdir drcl.inet.mac.Channel channel
# Capacity of the wireless channel is number of sensors and sinks ONLY
# which is equal to $node_num - $target_node_num
    ! channel setCapacity [expr $node_num - $target_node_num]
# create the node position tracker
    mkdir drcl.inet.mac.NodePositionTracker tracker
# maxX minX maxY minY dX dY
    ! tracker setGrid 100.0 0.0 100.0 0.0 100.0 100.0


    connect channel/.tracker@ -and tracker/.channel@


# For sink only
for {set i 0} {$i < [expr $sink_id + 1]} {incr i} {
    puts "create sink $i"
    set node$i [mkdir drcl.comp.Component n$i]

    cd n$i
    mkdir drcl.inet.sensorsim.MHTrAgg.AgTr_SinkApp app
    ! app setNid $i
    ! app setSinkNid $sink_id
    ! app setCoherentThreshold 1000.0

# create wireless agent layers
    mkdir drcl.inet.sensorsim.AC_Agg.WirelessACOAgent wireless_agent
# connect the sensor application to the wireless agent
# so that sinks can send through the wireless network protocol stack
    connect app/down@ -to wireless_agent/up@
# connect the wireless agent to the sensor application
# so that sinks can receive thru the wireless network protocol stack
    connect wireless_agent/.toSensorApp@ -to app/.fromWirelessAgent@
    mkdir drcl.inet.mac.LL ll
    mkdir drcl.inet.mac.ARP arp
    mkdir drcl.inet.core.queue.FIFO queue
    mkdir drcl.inet.mac.CSMA.Mac_CSMA mac
    mkdir drcl.inet.mac.WirelessPhy wphy

    connect wphy/.channelCheck@ -and mac/.wphyRadioMode@

    mkdir drcl.inet.mac.FreeSpaceModel propagation
    mkdir drcl.inet.mac.MobilityModel mobility

    set PD [mkdir drcl.inet.core.PktDispatcher pktdispatcher]
    set RT [mkdir drcl.inet.core.RT rt]
    set ID [mkdir drcl.inet.core.Identity id]

    ! pktdispatcher setRouteBackEnabled 1

    $PD bind $RT
    $PD bind $ID


    connect app/.setRoute@ -to rt/.service_rt@

    # ! wphy setRxThresh 0.0
    #! wphy setCSThresh 0.0
    ! wphy setTransmissionRange $trRng


    connect wphy/.mobility@ -and mobility/.query@
    connect wphy/.propagation@ -and propagation/.query@

    connect mac/down@ -and wphy/up@
    connect mac/up@ -and queue/output@

    connect ll/.mac@ -and mac/.linklayer@
    connect ll/down@ -and queue/up@
    connect ll/.arp@ -and arp/.arp@

    connect -c pktdispatcher/0@down -and ll/up@

    set nid $i
! arp setAddresses $nid $nid
    ! ll setAddresses $nid $nid
   ! mac setNode_num_  $nid       ;#set the MAc address

    ! mac setMacAddress $nid  ;#set MAC


    ! wphy setNid $nid

    #malihe set it to 1
    ! wphy  setLAMode     1

    ! mobility setNid $nid
    ! id setDefaultID $nid

    ! queue setMode "packet"
    ! queue setCapacity 40

# disable ARP
    ! arp setBypassARP [ expr 2>1]
    connect mobility/.report@ -and /MHTrAgg/tracker/.node@
    connect wphy/down@ -to /MHTrAgg/channel/.node@

    !  /MHTrAgg/channel attachPort $i [! wphy getPort .channel]
    # connect wphy/.channelCheck@ -and mac/.wphyRadioMode@

    ! mobility setTopologyParameters   600.0 600.0 600.0 0.0 0.0 0.0 100.0 100.0 100.0


    connect -c wireless_agent/down@ -and pktdispatcher/1111@up
    cd ..

}

# For sensor node only
for {set i [expr $sink_id + 1]} {$i < [expr $node_num - $target_node_num]} {incr i} {

    puts "create sensor $i"
    set node($i) [mkdir drcl.comp.Component n$i]

    cd n$i

    mkdir drcl.inet.sensorsim.MHTrAgg.AgTr_SourceApp app
    ! app setNid $i
    ! app setSinkNid $sink_id
    ! app setCoherentThreshold 1000.0

    #malihe
    #node num - sink node
    ! app setNodeNumber  [expr $node_num - 1]
    ! app setAreaNum  $ArNu
    ! app setDim  $d1 $d2 $d3
    ! app setPriodicSenseing  $PrSns

  # create nodes
  	mkdir drcl.inet.sensorsim.SensorAgent agent

  	! agent setDebugEnabled 0

  	# create sensor physical layers
  	mkdir drcl.inet.sensorsim.SensorPhy phy
  	! phy setRxThresh 0.0
  	! phy setDebugEnabled 0


# create mobility models
	mkdir drcl.inet.sensorsim.SensorMobilityModel mobility
	! mobility setNid $i
# connect phyiscal layers to sensor agents so that nodes can receive
	connect phy/.toAgent@ -to agent/.fromPhy@

	# connect sensor agent and sensor application
	connect agent/.toSensorApp@ -to app/.fromSensorAgent@

	# connect the sensor channel to the nodes so that they can receive
	! /MHTrAgg/chan attachPort $i [! phy getPort .channel]

	# connect the nodes to the propagation model
	connect phy/.propagation@ -and /MHTrAgg/seismic_Prop/.query@

    mkdir drcl.inet.sensorsim.AC_Agg.WirelessACOAgent wireless_agent
    connect app/down@ -to wireless_agent/up@
    connect wireless_agent/.toSensorApp@ -to app/.fromWirelessAgent@

    mkdir drcl.inet.mac.LL ll
    mkdir drcl.inet.mac.ARP arp
    mkdir drcl.inet.core.queue.FIFO queue
    mkdir drcl.inet.mac.CSMA.Mac_CSMA  mac
    mkdir drcl.inet.mac.WirelessPhy wphy
    #! wphy setRxThresh 0.0
   # ! wphy setCSThresh 0.0

#malihe set it to 10
    ! wphy setTransmissionRange $trRng
#malihe set it to 1
    ! wphy setMIT_uAMPS 1	;#turn on MH mode settings
#malihe set it to 1
    ! wphy  setLAMode     1
    connect wphy/.channelCheck@ -and mac/.wphyRadioMode@
    mkdir drcl.inet.mac.FreeSpaceModel propagation

		set PD [mkdir drcl.inet.core.PktDispatcher pktdispatcher]
		set RT [mkdir drcl.inet.core.RT rt]
		set ID [mkdir drcl.inet.core.Identity id]
    ! pktdispatcher setRouteBackEnabled 1
		$PD bind $RT
		$PD bind $ID

	        #**************
        	#NICHOLAS
        	# create route configuration request for testing
        	#this is to define the interfaces. So in this case each sensor
        	#only has 1 interface (hence array size 1) and its eth0.
        	#another example is (which has 3 interfaces 0, 2, and 4:
        	#set ifs [java::new drcl.data.BitSet [java::new {int[]} 3 {0 2 4}]]

        	set ifs [java::new drcl.data.BitSet [java::new {int[]} 1 {0}]]
        	set base_entry [java::new drcl.inet.data.RTEntry $ifs]

        	set key [java::new drcl.inet.data.RTKey $i 0 -1]
           	set entry_ [!!! [$base_entry clone]]

        	! rt add $key $entry_



    connect app/.setRoute@ -to rt/.service_rt@
	connect app/.energy@ -and wphy/.appEnergy@
    mkdir drcl.inet.sensorsim.CPUAvr cpu

	connect app/.cpu@ -and cpu/.reportCPUMode@
	connect cpu/.battery@ -and wphy/.cpuEnergyPort@

#	connect mac/.sensorApp@ -and app/.macSensor@

    connect wphy/.mobility@    -and  mobility/.query@
    connect wphy/.propagation@ -and  propagation/.query@

    connect mac/down@ -and  wphy/up@
    connect mac/up@   -and  queue/output@

    connect ll/.mac@ -and mac/.linklayer@
    connect ll/down@ -and queue/up@
    connect ll/.arp@ -and arp/.arp@

    connect -c pktdispatcher/0@down -and ll/up@

    set nid $i

    ! arp setAddresses $nid $nid
    ! ll setAddresses $nid $nid
    ! mac setMacAddress $nid
    ! mac setMacAddress $nid  	;#same as above
    ! wphy setNid $nid
    ! id setDefaultID $nid

    ! queue setMode "packet"
    ! queue setCapacity 40

# disable ARP
    ! arp setBypassARP [ expr 2>1]

   # ! mac setRTSThreshold 0

    connect mobility/.report@ -and /MHTrAgg/tracker/.node@
    connect wphy/down@ -to /MHTrAgg/channel/.node@

    ! /MHTrAgg/channel attachPort $i [! wphy getPort .channel]
     #connect wphy/.channelCheck@ -and mac/.wphyRadioMode@


# maxX maxY maxZ minX minY minZ dX dY dZ
    ! mobility setTopologyParameters 600.0 600.0 600.0 0.0 0.0 0.0 100.0 100.0 100.0

    connect -c wireless_agent/down@ -and pktdispatcher/1111@up
    cd ..
}

#***********************************************************
# FOR THE TARGET NODES ONLY , do the following
if { $target_node_num == 0 } {
	puts "No target agents .... "
} else {
	for {set i [expr $node_num - $target_node_num]} {$i < $node_num} {incr i} {
		puts "create target $i"

		set node$i [mkdir drcl.comp.Component n$i]

		cd n$i

		# create target agents
		mkdir drcl.inet.sensorsim.TargetAgent agent
		! agent setBcastRate 10.0
		! agent setSampleRate 1.0

		# create sensor physical layers
		mkdir drcl.inet.sensorsim.SensorPhy phy
		! phy setRxThresh 0.0
		! phy setNid $i
		! phy setRadius 100.0

		! phy setDebugEnabled 0

		# create mobility models
		mkdir drcl.inet.sensorsim.SensorMobilityModel mobility

		# connect target agents to phy layers so that nodes can send
		connect agent/down@ -to phy/up@

		# connect phy layers to sensor channel so that nodes can send
		# connect phy layers to sensor channel so that nodes can send
		connect phy/down@ -to /MHTrAgg/chan/.node@

		# connect the nodes to the propagation model
		connect phy/.propagation@ -and /MHTrAgg/seismic_Prop/.query@

		! mobility setNid $i

		# set the topology parameters
		! mobility setTopologyParameters 100.0 100.0 0.0 100.0 100.0 0.0

		cd ..
	}
}
#***********************************************
# for SENSORs and TARGETs only. Not SINKs
if { $target_node_num == 0 } {
	puts "No target agents .... "
   }

    for {set i [expr $sink_id + 1]} {$i < $node_num} {incr i} {
# connect the mobility model of each node to the node position tracker
    connect n$i/mobility/.report_sensor@ -and /MHTrAgg/nodetracker/.node@
    connect n$i/phy/.mobility@ -and n$i/mobility/.query@
}
#***********************************************
#Plotters

#Graph # 1
#To plot the total number of received packets at the sink
#red line-> actual packets received
#blue line-> what the sink actually would have received if it
#	     wasn't for tree aggregation
set sinkPlot1_ [mkdir drcl.comp.tool.Plotter .sinkPlot1]
connect -c n0/app/.PacketsReceivedPlot@ -to $sinkPlot1_/0@0
connect -c n0/app/.theo_PacketsReceivedPlot@ -to $sinkPlot1_/1@0

#Graph # 2
# 	In order to graph total # of sensors still
#	alive we created a new component that will
#	have a connected port to a plotter
mkdir drcl.inet.sensorsim.AliveSensors	liveSensors
set numNodesPlot_ [mkdir drcl.comp.tool.Plotter .numNodesPlot]
connect -c liveSensors/.plotter@ -to $numNodesPlot_/0@0

#Graph # 3
#Output remaining energy levels of the sensors to a plotter
set plot_ [mkdir drcl.comp.tool.Plotter .plot]
for {set i [expr $sink_id + 1]} {$i < [expr $node_num - $target_node_num]} {incr i} {
	connect -c n$i/app/.plotter@ -to $plot_/$i@0
}
#******************Other Functions***********************
 #sensorLocPrintOut()
#	Goes throught all sensors and prints their
#	(X,Y,Z) Coordinates
proc sensorLocPrintOut { } {
	global sink_id node_num	target_node_num
	for {set i [expr $sink_id + 1]} {$i < [expr $node_num - $target_node_num]} {incr i} {
		script [! n$i/app printNodeLoc]
	}
}
#*************************************************
 #wsnLoop()
#
#	This method is called periodically to check
#	if the simulation should continue or not. If
# 	all nodes are dead then stop the simulator and
#	display the cummulative statistics... o.w keep
#	running
proc wsnLoop { } {
	global sim node_num node sink_id target_node_num
	set dead_sensors 0
	set live_sensors 0
	for {set i [expr $sink_id + 1]} {$i < [expr $node_num - $target_node_num]} {incr i} {
		if { [! $node($i)/app isSensorDead] == 0 } {
			incr live_sensors
		} else {
			incr dead_sensors
		}
	}

        script [! liveSensors setLiveNodes $live_sensors]
    	script [! liveSensors updateGraph]


          set filenameUe "MY_RESULT/tag/UsedEnergyTag.txt"
           set filenameT "MY_RESULT/tag/TotalInPktsTag.txt"
           set filenameV "MY_RESULT/tag/VirtualPktsTag.txt"

           set outUe [open $filenameUe a+]
           set outT [open $filenameT a+]
           set outV [open $filenameV a+]

           for {set i [expr $sink_id + 1]} {$i < [expr $node_num - $target_node_num]} {incr i} {
             if { [! $node($i)/app isSensorDead] == 0 } {
                set Energy [! $node($i)/app UsedEnergy]
                puts -nonewline $outUe "$Energy,"
             }
           }
           puts $outUe " "
           set ttlPkt [! n0/app getTotalPkts]
           set vrtPkt [! n0/app getVirtualPkts]

            if { $vrtPkt != 0 } {
                puts $outV "$vrtPkt"
             }
             if { $ttlPkt != 0 } {
                 puts $outT "$ttlPkt"
             }

                close $outUe
                close $outT
                close $outV


          if { $dead_sensors != 0 } {
           puts "some nodes dead at [! $sim getTime]"
           # $sim stop
           puts "----------------------------------------------"
           puts "Simulation Terminated\n"
           puts "Results:"
           puts "Remaining Energy in all nodes are:"

           #open a file for writting
           set filenameE "MY_RESULT/tag/EnrgyTag.txt"
            set outE [open $filenameE w]

            for {set i [expr $sink_id + 1]} {$i < [expr $node_num - $target_node_num]} {incr i} {
             set Energy    [! $node($i)/app RemainEnergy]
             puts "node($i)  Energy: $Energy"
            puts $outE "$Energy"
             }
             close $outE

            # set end 1
             $sim stop
        }

}
#******************Positioning************************
# set the position of sink nodes
 ! n0/mobility setPosition 0.0 0.0 0.0 0.0
#for {set i [expr $sink_id + 1]} {$i < [expr $node_num - $target_node_num]} {incr i} {
#   ! n$i/mobility setPosition 0.0 [expr rand()*$d1] [expr rand() * $d2] $d3
#}

# for the target we can include random mobility They will be randomly
# placed on the grid (2D only)
#set the position of target nodes args=> (speed(m/sec), xCoord,yCoord,zCoord
#for {set i [expr $node_num - $target_node_num]} {$i < $node_num} {incr i} {
#   ! n$i/mobility setPosition 0.0 [expr rand()*$d1] [expr rand() * $d2] 0.0
#}

              ! n1/mobility setPosition 0.0 54.8230224544 10.5383915876 0.0
              ! n2/mobility setPosition 0.0 85.5601499256 9.43979882143 0.0
              ! n3/mobility setPosition 0.0 54.698791846 22.5945561764 0.0
              ! n4/mobility setPosition 0.0 46.7056564273 81.9675734183 0.0
              ! n5/mobility setPosition 0.0 29.0064408113 11.2507159874 0.0
              ! n6/mobility setPosition 0.0 90.7836003652 99.9713375233 0.0
              ! n7/mobility setPosition 0.0 18.26975407 59.7566542028 0.0
              ! n8/mobility setPosition 0.0 30.0871868758 75.3498214648 0.0
              ! n9/mobility setPosition 0.0 4.4493586777 80.3712961173 0.0
              ! n10/mobility setPosition 0.0 0.373842707078 83.1743778583 0.0
              ! n11/mobility setPosition 0.0 11.7686636801 95.93047099 0.0
              ! n12/mobility setPosition 0.0 3.42592867251 79.5831988471 0.0
              ! n13/mobility setPosition 0.0 18.7474123755 87.7597954067 0.0
              ! n14/mobility setPosition 0.0 78.8814007672 59.702694211 0.0
              ! n15/mobility setPosition 0.0 23.1816045582 13.2278100183 0.0
              ! n16/mobility setPosition 0.0 19.8029781784 28.6542442761 0.0
              ! n17/mobility setPosition 0.0 91.8835478797 86.7892141392 0.0
              ! n18/mobility setPosition 0.0 66.3220382139 74.4962605529 0.0
              ! n19/mobility setPosition 0.0 58.6511125596 49.2487884356 0.0
              ! n20/mobility setPosition 0.0 24.3872370219 76.292626735 0.0
              ! n21/mobility setPosition 0.0 50.1775344602 33.8216721238 0.0
              ! n22/mobility setPosition 0.0 40.8433842663 54.7593635762 0.0
              ! n23/mobility setPosition 0.0 40.6236250608 61.2663974339 0.0
              ! n24/mobility setPosition 0.0 4.34167231635 70.4866209396 0.0
              ! n25/mobility setPosition 0.0 68.6381312407 1.07176210781 0.0
              ! n26/mobility setPosition 0.0 13.1057459922 68.2728910205 0.0
              ! n27/mobility setPosition 0.0 62.4793820374 90.9739027223 0.0
              ! n28/mobility setPosition 0.0 98.3830544159 23.99556759 0.0
              ! n29/mobility setPosition 0.0 93.5044850658 29.8805012041 0.0
              ! n30/mobility setPosition 0.0 1.58373755477 17.8770829541 0.0
              ! n31/mobility setPosition 0.0 60.1332096197 58.8540788548 0.0
              ! n32/mobility setPosition 0.0 60.5033131598 79.1842768338 0.0
              ! n33/mobility setPosition 0.0 50.1407448436 15.4985861459 0.0
              ! n34/mobility setPosition 0.0 84.737354603 80.7188127566 0.0
              ! n35/mobility setPosition 0.0 41.0859997576 32.3979252635 0.0
              ! n36/mobility setPosition 0.0 11.9299029056 5.87813384173 0.0
              ! n37/mobility setPosition 0.0 93.7954780151 20.5989999793 0.0
              ! n38/mobility setPosition 0.0 7.39265131177 48.2905969249 0.0
              ! n39/mobility setPosition 0.0 20.0625160802 90.7077606724 0.0
              ! n40/mobility setPosition 0.0 25.3336212716 82.1727113715 0.0
              ! n41/mobility setPosition 0.0 76.7600212138 5.67654026937 0.0
              ! n42/mobility setPosition 0.0 5.61230723076 26.0476274071 0.0
              ! n43/mobility setPosition 0.0 82.4738319416 37.6934423753 0.0
              ! n44/mobility setPosition 0.0 13.6860009347 20.6177087597 0.0
              ! n45/mobility setPosition 0.0 21.8311237739 15.6972672863 0.0
              ! n46/mobility setPosition 0.0 23.9712814912 85.3280227097 0.0
              ! n47/mobility setPosition 0.0 8.07768251192 61.6099777918 0.0
              ! n48/mobility setPosition 0.0 78.89674738 17.6332153462 0.0
              ! n49/mobility setPosition 0.0 61.4503236308 95.5892628504 0.0
              ! n50/mobility setPosition 0.0 68.7407265272 25.3907421722 0.0
              ! n51/mobility setPosition 0.0 25.0 25.0 0.0




#***********************************************

    puts "simulation begins..."
    set sim [attach_simulator .]
    $sim stop

#*******************running *************************
 script {run n0} -at 0.3 -on $sim
 for {set i [expr $sink_id + 1]} {$i < $node_num} {incr i} {
	script puts "run n$i" -at 0.1 -on $sim
}

#*********print out all the node locations**************
script "sensorLocPrintOut" -at 0.002 -on $sim
puts "sink location  is 0.0 , 0.0"
#*******Check if Sensor Status**************************
 script "wsnLoop" -at 1.0 -period 1.0 -on $sim


  $sim resumeFor 100000.0
