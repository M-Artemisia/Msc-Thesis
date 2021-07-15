# tcpnam3.tcl
#
# Use drcl.inet.InetUtil.setNamTraceOn()
# to do one-step setup of a NamTrace component
#
# Topology:
# 
# h0 ----- n1 ----- h2

cd [mkdir -q drcl.comp.Component /example2]

# create the topology
puts "create topology..."
set link_ [java::new drcl.inet.Link]
$link_ setPropDelay 0.3; # 300ms
set adjMatrix_ [java::new {int[][]} 3 {{1} {0 2} {1}}]
java::call drcl.inet.InetUtil createTopology [! .] $adjMatrix_ $link_

puts "create builders..."
# NodeBuilder:
set rb [mkdir drcl.inet.NodeBuilder .routerBuilder]
$rb setBandwidth 1.0e6; #10Mbps
set hb1 [cp $rb .hostBuilder1]
set hb2 [cp $rb .hostBuilder2]
# create TCP's at host builders
[mkdir drcl.inet.transport.TCP $hb1/tcp] setMSS 512; # bytes
mkdir drcl.inet.transport.TCPSink $hb2/tcpsink

puts "build..."
$rb build [! n?]
$hb1 build [! h0] {
	tcp 				drcl.inet.transport.TCP
	source	-/tcp		drcl.inet.application.BulkSource
}
$hb2 build [! h2] {
	tcpsink				drcl.inet.transport.TCPSink
	sink	-/tcpsink	drcl.inet.application.BulkSink
}
! h?/tcp* setMSS 512
! h?/s* setDataUnit 512

# Configure the bottleneck bandwidth and buffer size
! n1 setBandwidth 1 1.0e4; # 10Kbps at interface 1
! n1 setBufferSize 1 6000; # ~10 packets at interface 1

! h0/tcp setPeer 2

puts "setup static routes..."
java::call drcl.inet.InetUtil setupRoutes [! h0] [! h2] "bidirection"

if 0 {
puts "Set up TrafficMonitor & Plotter..."
set plot_ [mkdir drcl.comp.tool.Plotter .plot]
if 0 {
	setflag plot false $plot_
	set file [mkdir drcl.comp.io.FileComponent .file]
	$file open "test.plot"
	connect -c $plot_/.output@ -to $file/in@
} else {
	#[$plot_ getPlot 1] setStepwise true
	#set p [$plot_ getPlot 2]
	#foreach i {0 1 2} { $p setConnected false $i }
	#foreach i {0 2} { $p setMarksStyle dots $i }
	#$p setMarksStyle points 1
}
set tm_ [mkdir drcl.net.tool.TrafficMonitor .tm]
connect -c h2/csl/6@up -to $tm_/in@
connect -c $tm_/bytecount@ -to $plot_/0@0
connect -c h0/tcp/cwnd@ -to $plot_/0@1
connect -c h0/tcp/rtt@ -to $plot_/0@3
connect -c h0/tcp/seqno@ -to $plot_/0@2
connect -c h0/tcp/ack@ -to $plot_/1@2
connect -c h2/tcpsink/seqno@ -to $plot_/2@2
}

# flags
#setflag garbagedisplay true .../ni*

# for better visualization in NAM
! .link0 setPropDelay .1
! .link1 setPropDelay .6

if 1 {
puts "Set up NamTrace..."

# this sets up node, link and queue events, and create/connect event ports on $nam
set nam [java::call drcl.inet.InetUtil setNamTraceOn [! .] "tcpnam3.nam" \
	[_to_string_array "red blue yellow green black orange"]]; # six flows at most
}

puts "simulation begins..."	
attach_simulator .
#! h0/tcp setImplementation tahoe
#setflag sack true h0,h2/tcp*
#setflag debug true -at "ack dupack send timeout" h0/tcp
#setflag debug true -at "send" h0/tcp
run h?
rt . stopAt 500.0
