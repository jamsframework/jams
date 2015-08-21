for f in `find . -name *.java -type f`; do
    iconv -f cp1252 -t utf-8 $f > $f.converted
    mv $f.converted $f
done