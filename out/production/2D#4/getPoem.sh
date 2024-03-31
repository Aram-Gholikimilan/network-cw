for part {1..7}
do
echo =========LMAO=========
for p in {20000..20010}
do
echo ================ PART $part  NODE - $p ==================
java CmdLineGet martin.brain@city.ac.uk:martins-implementation-1.0,fullNode-$p 10.0.0.164:$p test/jabberwocky/$part
done
done