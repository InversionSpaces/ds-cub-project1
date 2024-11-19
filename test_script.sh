#!/bin/bash

case $1 in
	"1" )	echo CoarseGrainedListBasedSet
			OUTPUT="CoarseGrainedListBasedSet"
	;;
	"2" )	echo HandOverHandListBasedSet
			OUTPUT="HandOverHandListBasedSet"
	;;
  "3" )	echo HandOverHandListBasedSetOptimized
			OUTPUT="HandOverHandListBasedSetOptimized"
	;;
	"4" )	echo LazyLinkedListSortedSet
			OUTPUT="LazyLinkedListSortedSet"
	;;
	*)		echo "Specify algorithm"
			exit 0
esac

echo "Who I am: $OUTPUT on $(uname -n)"
echo "started on" "$(date)"

touch results2.csv

for t in 1 4 6 8 10 12
do
	for ur in 0 10 100
	do
    for ls in 100 1000 10000
    do
      echo "→ $OUTPUT"
      echo "threads: $t"
      echo "update ratio: $ur"
      echo "list size: $ls"
      r=$((ls * 2))
      echo "range: $r"
      tp=$(java -cp bin contention.benchmark.Test -b linkedlists.lockbased.$OUTPUT -d 2000 -t $t -u $ur -i $ls -r $r -W 0 | grep Throughput)
      echo "$tp"
      tpn=$(echo $tp | awk -F ':' '{print $NF}')
      echo "$OUTPUT;$t;$ur;$ls;$r;$tpn" >> results2.csv
  #		echo "→ $OUTPUT	$i	$j	without -W 0"
  #		java -cp bin contention.benchmark.Test -b linkedlists.lockbased.$OUTPUT -d 3000 -t $i -u $j -i 1024 -r 2048 | grep Throughput
	  done
	done
	# wait
done
echo "finished on" "$(date)"
echo "DONE \o/"