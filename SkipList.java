package SkipList;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.*;

public class SkipList{
	SkipListNode head;
	int maxLevel;
	
	SkipList(int maxL){
		maxLevel = maxL;
		head = new SkipListNode(Integer.MIN_VALUE, 0, maxLevel);
		SkipListNode wartownik = new SkipListNode(Integer.MAX_VALUE, 0, 0);
		for(int i=0; i<maxLevel; i++)
			head.forward[i] = wartownik;
	}

	//znajdz liste poprzednikow wezla jesli istnieje lub potencjalnych poprzednikow jesli nie istnieje
	private SkipListNode[] find(int szukany){
		SkipListNode previous[] = new SkipListNode[maxLevel];	//lista poprzednikow wyszukiwanego wezla
		SkipListNode biezacy=head;
		for (int i=maxLevel-1; i>=0;i--){			//wyszukuje poprzednik szukanego elementu
			while(biezacy.forward[i].klucz<szukany){
				biezacy = biezacy.forward[i];
			}
			previous[i] = biezacy;
		}
		biezacy = biezacy.forward[0];
		return previous;		//zwroc referencje do tablicy poprzednikow wezla o podanym kluczu
	}
	
	//znajdz wezel o podanym kluczu
	Double get(int szukany){
		SkipListNode found = find(szukany)[0].forward[0];
		if (found.klucz == szukany) return found.dane;
		else{
			System.out.println("Brak wezla o kluczu "+ szukany);
			return null;
		}
	}

	private int losujPoziom(){		//uwaga! poza ta metoda poziomy liczone sa od zera, tutaj od 1
		int level=1;
		Random los = new Random();
		while(los.nextInt(2) == 1 && level <maxLevel)	//losuje 0->nie zwiekszaj poziomu lub 1->zwieksz poziom
			level++;
		return level;
	}
	
	Double put(int key, Double value){
		SkipListNode[] previous = find(key);
		SkipListNode toInsert = previous[0].forward[0];
		
		if (toInsert.klucz != key){
			int nowyPoziom = losujPoziom();
			SkipListNode nowy = new SkipListNode(key, value, nowyPoziom);
			for(int i=0;i<nowyPoziom;i++){			//ustawienie nowych wskaznikow w poprzednikach i nowym wezle
				nowy.forward[i]=previous[i].forward[i];
				previous[i].forward[i] = nowy;
			}
			return null;
		}
		else{
			Double tmp = toInsert.dane;
			toInsert.dane = value;
			return tmp;
		}
	}
	
	Double remove(int key){
		SkipListNode[] previous = find(key);
		SkipListNode toDel = previous[0].forward[0];
		
		if (toDel.klucz != key){
			System.out.println("Brak wezla o kluczu rownym" + toDel.klucz);
			return null;
		}
		else{
			Double tmp = toDel.dane;
			for(int i=0;i<maxLevel;i++){
				if(previous[i].forward[i]!=toDel) break;
				previous[i].forward[i] = toDel.forward[i];
			}
			return tmp;
		}	
	}
	
	Integer higherKey(int key){
		SkipListNode found = find(key)[0].forward[0];
		if(found.klucz != key){
			System.out.println("Wezel o podanym kluczu nie istnieje");
			return null;
		}
		return found.forward[0].klucz;
	}
	
	Integer lowerKey(int key){
		SkipListNode poprzednik = find(key)[0];
		if (poprzednik.forward[0].klucz != key){
			System.out.println("Brak wezla o kluczu rownym "+ key);
			return null;
		}
		return poprzednik.klucz;
	}
	
	boolean containsKey(int key){
		if(find(key)[0].forward[0].klucz!=key) return false;
		else return true;
	}

	
//----------------------------------------------------------------------------	
	public static void main(String[] args) throws Exception{
		List<Integer> lista = new ArrayList<Integer>();
		final int N = 100000;
		for (int i=0;i<N;i++)
			lista.add(i);
		Collections.shuffle(lista);
		int level = (int)(Math.log10(N)/Math.log10(2.0));
		//SkipList sl  = new SkipList(level);
		SkipList sl  = new SkipList(level);
		//System.out.println(sl.maxLevel);
		Random los = new Random();
		
//----------------------------------------------------------------------------
		//testy:
		//moja lista
		System.out.println("Moja lista z przeskokami zawiera " + N + " elementow");
		//wstawianie
		long mojaPut, mojaContains, mojaGet, mojaRemove, start, end,
			concurrentPut, concurrentContains, concurrentGet, concurrentRemove;
		start = System.currentTimeMillis();
		for(int i=0; i<N; i++)
			sl.put(lista.get(i), los.nextDouble());
		end = System.currentTimeMillis();
		System.out.println("Czas wstawiania: "+ (mojaPut=end-start)+"ms.");
		//czy lista zawiera element
		start = System.currentTimeMillis();
		for(int i=0; i<N; i++)
			sl.containsKey(i);
		end = System.currentTimeMillis();
		System.out.println("Czas sprawdzenia zawierania: "+ (mojaContains=end-start)+"ms.");
		//pobieranie elementu
		start = System.currentTimeMillis();
		for(int i=0; i<N; i++)
			sl.get(i);
		end = System.currentTimeMillis();
		System.out.println("Czas pobrania: "+ (mojaGet=end-start)+"ms.");		
		//usuwanie elementu
		start = System.currentTimeMillis();
		for(int i=0; i<N; i++)
			sl.remove(i);
		end = System.currentTimeMillis();
		System.out.println("Czas usuwania: "+ (mojaRemove=end-start)+"ms.");
		for(int i=0; i<N; i++)
			sl.put(lista.get(i), los.nextDouble());
		//for(int i=0; i<N; i++)v
		//	System.out.println(sl.find(i).klucz);
		//wyzszy klucz
		boolean nextIsPlusOne = true;
		for (int i=0; i<N-1;i=sl.higherKey(i))
			if (sl.higherKey(i) != i+1) nextIsPlusOne=false;
		System.out.println("Dla elementów w petli higherKey(x)=x+1 jest spełnione? "+nextIsPlusOne);
		boolean prevIsMinusOne = true;
		for (int i=N-1; i>0;i=sl.lowerKey(i))
			if (sl.lowerKey(i) != i-1) nextIsPlusOne=false;
		System.out.println("Dla elementów w petli LowerKey(x)=x-1 jest spełnione? "+prevIsMinusOne);
//----------------------------------------------------------------------------
		ConcurrentSkipListMap<Integer,Double> concurrent = new ConcurrentSkipListMap<Integer,Double>();
		//lista z JVM
		System.out.println("\nConcurrentSkipListMap zawiera " + N + " elementow");
		//wstawianie
		start = System.currentTimeMillis();
		for(int i=0; i<N; i++)
			concurrent.put(i, los.nextDouble());
		end = System.currentTimeMillis();
		System.out.println("Czas wstawiania: "+ (concurrentPut=end-start)+"ms.");
		
		//czy lista zawiera element
		start = System.currentTimeMillis();
		for(int i=0; i<N; i++)
			concurrent.containsKey(i);
		end = System.currentTimeMillis();
		System.out.println("Czas sprawdzenia zawierania: "+ (concurrentContains=end-start)+"ms.");
		//pobieranie elementu
		start = System.currentTimeMillis();
		for(int i=0; i<N; i++)
			concurrent.get(i);
		end = System.currentTimeMillis();
		System.out.println("Czas pobrania: "+ (concurrentGet=end-start)+"ms.");		
		//usuwanie elementu
		start = System.currentTimeMillis();
		for(int i=0; i<N; i++)
			concurrent.remove(i);
		end = System.currentTimeMillis();
		System.out.println("Czas usuwania: "+ (concurrentRemove=end-start)+"ms.");
		for(int i=0; i<N; i++)
			sl.put(lista.get(i), los.nextDouble());
		System.out.println("\nPorownanie wynikow(o ile ms moja lista jest wolniejsza od dostarczonej:\n"
				+ "Czas wstawiania: "+ (mojaPut-concurrentPut)+"\n"
				+ "Czas sprawdzenia zawierania: "+ (mojaContains-concurrentContains)+"\n"
				+ "Czas pobrania: "+ (mojaGet-concurrentGet)+"\n"
				+ "Czas usuwania: "+ (mojaRemove-concurrentRemove)+"."
				);
	}
}
