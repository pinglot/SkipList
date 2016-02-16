package SkipList;
class SkipListNode{
	int klucz;
	double dane;
	SkipListNode forward[];
	
	SkipListNode(int key, double data, int level){
		klucz = key;
		dane = data;
		forward = new SkipListNode[level];
	}
}
