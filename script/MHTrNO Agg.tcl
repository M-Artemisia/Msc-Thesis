

    set ArNu 10
    set d1 100
    set d2 100
    set d3 0
    set trRng 25
    set PrSns 10

 cd [mkdir -q drcl.comp.Component /MHTrNoAgg]
# TOTAL number of nodes (sensor nodes + target nodes)
    set node_num 102
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
    mkdir drcl.inet.sensorsim.MHTrNoAgg.Tr_SinkApp app
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
    connect mobility/.report@ -and /MHTrNoAgg/tracker/.node@
    connect wphy/down@ -to /MHTrNoAgg/channel/.node@

    !  /MHTrNoAgg/channel attachPort $i [! wphy getPort .channel]
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

    mkdir drcl.inet.sensorsim.MHTrNoAgg.Tr_SourceApp app
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
	! /MHTrNoAgg/chan attachPort $i [! phy getPort .channel]

	# connect the nodes to the propagation model
	connect phy/.propagation@ -and /MHTrNoAgg/seismic_Prop/.query@

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

    connect mobility/.report@ -and /MHTrNoAgg/tracker/.node@
    connect wphy/down@ -to /MHTrNoAgg/channel/.node@

    ! /MHTrNoAgg/channel attachPort $i [! wphy getPort .channel]
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
		connect phy/down@ -to /MHTrNoAgg/chan/.node@

		# connect the nodes to the propagation model
		connect phy/.propagation@ -and /MHTrNoAgg/seismic_Prop/.query@

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
    connect n$i/mobility/.report_sensor@ -and /MHTrNoAgg/nodetracker/.node@
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


          set filenameUe "MY_RESULT/tree/UsedEnergyTree.txt"
           set filenameT "MY_RESULT/tree/TotalInPktsTree.txt"
           set filenameV "MY_RESULT/tree/VirtualPktsTree.txt"

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
           set filenameE "MY_RESULT/tree/EnrgyTree.txt"
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
# for {set i [expr $sink_id + 1]} {$i < [expr $node_num - $target_node_num]} {incr i} {
#   ! n$i/mobility setPosition 0.0 [expr rand()*$d1] [expr rand() * $d2] $d3
#}

# for the target we can include random mobility They will be randomly
# placed on the grid (2D only)
#set the position of target nodes args=> (speed(m/sec), xCoord,yCoord,zCoord
#for {set i [expr $node_num - $target_node_num]} {$i < $node_num} {incr i} {
#   ! n$i/mobility setPosition 0.0 [expr rand()*$d1] [expr rand() * $d2] 0.0
#}

   ! n1/mobility setPosition 0.0 50.8864267966 48.1751707607 0.0
   ! n2/mobility setPosition 0.0 80.0949748047 56.2415428256 0.0
   ! n3/mobility setPosition 0.0 51.6102696544 13.8020808407 0.0
   ! n4/mobility setPosition 0.0 71.5726898385 22.1981158118 0.0
   ! n5/mobility setPosition 0.0 83.7324485107 91.2621193525 0.0
   ! n6/mobility setPosition 0.0 42.4399570294 88.3577929755 0.0
   ! n7/mobility setPosition 0.0 29.426540029 71.8582668676 0.0
   ! n8/mobility setPosition 0.0 21.8912442782 26.1425843584 0.0
   ! n9/mobility setPosition 0.0 78.4153117698 26.1449146206 0.0
   ! n10/mobility setPosition 0.0 17.5800288178 67.5443406531 0.0
   ! n11/mobility setPosition 0.0 17.7333572031 44.5345119781 0.0
   ! n12/mobility setPosition 0.0 91.5428151803 60.0947350078 0.0
   ! n13/mobility setPosition 0.0 12.2112757118 34.9108874495 0.0
   ! n14/mobility setPosition 0.0 47.2853632864 25.1007545391 0.0
   ! n15/mobility setPosition 0.0 68.3815380411 88.5098569973 0.0
   ! n16/mobility setPosition 0.0 85.1665535407 94.2653585199 0.0
   ! n17/mobility setPosition 0.0 17.8806433537 19.9728463404 0.0
   ! n18/mobility setPosition 0.0 83.6284426896 43.2362841644 0.0
   ! n19/mobility setPosition 0.0 72.2279503347 35.1612745482 0.0
   ! n20/mobility setPosition 0.0 55.5413320919 83.1684689425 0.0
   ! n21/mobility setPosition 0.0 12.4575162364 73.4753859106 0.0
   ! n22/mobility setPosition 0.0 0.810999237379 30.464182622 0.0
   ! n23/mobility setPosition 0.0 11.5173282155 71.7353185507 0.0
   ! n24/mobility setPosition 0.0 55.4988818967 69.7080382471 0.0
   ! n25/mobility setPosition 0.0 82.9988182443 61.1382322671 0.0
   ! n26/mobility setPosition 0.0 50.2697124846 83.0577281225 0.0
   ! n27/mobility setPosition 0.0 51.2365545385 32.7721281595 0.0
   ! n28/mobility setPosition 0.0 1.15797738599 62.1259262609 0.0
   ! n29/mobility setPosition 0.0 50.4426668633 89.9019714398 0.0
   ! n30/mobility setPosition 0.0 82.4339884717 68.0442441572 0.0
   ! n31/mobility setPosition 0.0 19.6115495728 11.3136706926 0.0
   ! n32/mobility setPosition 0.0 48.8633312978 46.0091223689 0.0
   ! n33/mobility setPosition 0.0 75.3196538311 97.4219393904 0.0
   ! n34/mobility setPosition 0.0 70.5353351638 87.378098577 0.0
   ! n35/mobility setPosition 0.0 63.7027831579 52.6765350032 0.0
   ! n36/mobility setPosition 0.0 34.5237988208 41.4867812961 0.0
   ! n37/mobility setPosition 0.0 68.333243238 76.819100453 0.0
   ! n38/mobility setPosition 0.0 98.6213130404 28.4082705753 0.0
   ! n39/mobility setPosition 0.0 57.8035590042 4.41618422252 0.0
   ! n40/mobility setPosition 0.0 22.808227885 37.8860625149 0.0
   ! n41/mobility setPosition 0.0 51.0526881791 42.5302260753 0.0
   ! n42/mobility setPosition 0.0 5.50964819524 0.657217391141 0.0
   ! n43/mobility setPosition 0.0 45.8526929123 46.209776423 0.0
   ! n44/mobility setPosition 0.0 47.7123419976 1.33195431034 0.0
   ! n45/mobility setPosition 0.0 86.156093835 25.4690843753 0.0
   ! n46/mobility setPosition 0.0 58.9010955109 50.7122513609 0.0
   ! n47/mobility setPosition 0.0 20.8086219713 30.509471861 0.0
   ! n48/mobility setPosition 0.0 72.6935670118 60.7807677988 0.0
   ! n49/mobility setPosition 0.0 42.3643942188 18.3736358855 0.0
   ! n50/mobility setPosition 0.0 5.69832725716 71.7862110454 0.0
   ! n51/mobility setPosition 0.0 10.8490393548 39.8044354002 0.0
   ! n52/mobility setPosition 0.0 93.1457711817 0.976251578413 0.0
   ! n53/mobility setPosition 0.0 7.86027838842 7.69887417913 0.0
   ! n54/mobility setPosition 0.0 94.9783286988 0.770440232367 0.0
   ! n55/mobility setPosition 0.0 48.7889853999 96.4776160645 0.0
   ! n56/mobility setPosition 0.0 99.2931955956 20.7383758485 0.0
   ! n57/mobility setPosition 0.0 49.8828857904 81.6614788406 0.0
   ! n58/mobility setPosition 0.0 84.4748737684 69.2034252776 0.0
   ! n59/mobility setPosition 0.0 1.96864036935 86.9386876407 0.0
   ! n60/mobility setPosition 0.0 78.5231768985 39.034132957 0.0
   ! n61/mobility setPosition 0.0 46.6726084457 26.5301470768 0.0
   ! n62/mobility setPosition 0.0 30.4710000895 1.53453447927 0.0
   ! n63/mobility setPosition 0.0 90.9209931693 9.13219587371 0.0
   ! n64/mobility setPosition 0.0 84.816049498 3.34391212247 0.0
   ! n65/mobility setPosition 0.0 1.13104227983 37.9318407913 0.0
   ! n66/mobility setPosition 0.0 49.6244381413 72.5607832766 0.0
   ! n67/mobility setPosition 0.0 20.4481799716 23.691361036 0.0
   ! n68/mobility setPosition 0.0 29.0845297412 7.79523696182 0.0
   ! n69/mobility setPosition 0.0 80.7049321852 1.80431669662 0.0
   ! n70/mobility setPosition 0.0 14.5476173212 8.15252485133 0.0
   ! n71/mobility setPosition 0.0 25.1507200883 87.359145045 0.0
   ! n72/mobility setPosition 0.0 43.103289112 49.0084165935 0.0
   ! n73/mobility setPosition 0.0 24.6399819966 80.3571802938 0.0
   ! n74/mobility setPosition 0.0 49.8355117859 12.4334999884 0.0
   ! n75/mobility setPosition 0.0 0.75190756505 5.15640951002 0.0
   ! n76/mobility setPosition 0.0 76.6624214485 60.2884422803 0.0
   ! n77/mobility setPosition 0.0 87.5980072597 44.943603801 0.0
   ! n78/mobility setPosition 0.0 12.588481704 74.6586931286 0.0
   ! n79/mobility setPosition 0.0 3.88266979898 31.5080122703 0.0
   ! n80/mobility setPosition 0.0 18.2502970184 11.5553358158 0.0
   ! n81/mobility setPosition 0.0 94.6087492605 61.8435788256 0.0
   ! n82/mobility setPosition 0.0 4.93210884972 27.8215025216 0.0
   ! n83/mobility setPosition 0.0 75.4200656784 52.3292416485 0.0
   ! n84/mobility setPosition 0.0 32.0984529015 64.6326485391 0.0
   ! n85/mobility setPosition 0.0 75.8698059134 89.6096948952 0.0
   ! n86/mobility setPosition 0.0 16.9574466613 78.3223137624 0.0
   ! n87/mobility setPosition 0.0 68.0527660847 82.2957172442 0.0
   ! n88/mobility setPosition 0.0 44.9131423351 20.1817008761 0.0
   ! n89/mobility setPosition 0.0 64.4784138373 80.210615499 0.0
   ! n90/mobility setPosition 0.0 3.80317387348 85.5354850579 0.0
   ! n91/mobility setPosition 0.0 86.9020906216 40.0590975024 0.0
   ! n92/mobility setPosition 0.0 86.9572793073 41.7080143195 0.0
   ! n93/mobility setPosition 0.0 24.6870853122 30.2005263186 0.0
   ! n94/mobility setPosition 0.0 70.6422303667 86.5966680863 0.0
   ! n95/mobility setPosition 0.0 12.7364393849 80.245836815 0.0
   ! n96/mobility setPosition 0.0 86.6125785683 65.7732978769 0.0
   ! n97/mobility setPosition 0.0 97.6222184476 95.3413146992 0.0
   ! n98/mobility setPosition 0.0 63.9073218051 9.6422064163 0.0
   ! n99/mobility setPosition 0.0 39.8162828478 58.3533818174 0.0
   ! n100/mobility setPosition 0.0 11.6897476426 58.8603433496 0.0
   ! n101/mobility setPosition 0.0 50.0 50.0 0.0
   



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
