package Miner;

public class SortFunctions {

	public static void quickSortHighToLow(String[] content, float[] arr, int low, int high) {
		if (arr == null || arr.length == 0)
			return;

		if (low >= high)
			return;

		// pick the pivot
		int middle = (high + low) / 2;
		float pivot = arr[middle];

		// make left < pivot and right > pivot
		int i = low, j = high;
		while (i <= j) {
			while (arr[i] > pivot) {
				i++;
			}

			while (arr[j] < pivot) {
				j--;
			}

			if (i <= j) {
				float temp = arr[i];
				arr[i] = arr[j];
				arr[j] = temp;
				String temp1 = content[i];
				content[i] = content[j];
				content[j] = temp1;
				i++;
				j--;
			}
		}
		// recursively sort two sub parts
		if (low < j)
			quickSortHighToLow(content, arr, low, j);

		if (high > i)
			quickSortHighToLow(content, arr, i, high);
	}

	public static void quickSortLowToHigh(String[] content, float[] arr, int low, int high) {
		if (arr == null || arr.length == 0)
			return;

		if (low >= high)
			return;

		// pick the pivot
		int middle = (high + low) / 2;
		float pivot = arr[middle];

		// make left < pivot and right > pivot
		int i = low, j = high;
		while (i <= j) {
			while (arr[i] < pivot) {
				i++;
			}

			while (arr[j] > pivot) {
				j--;
			}

			if (i <= j) {
				float temp = arr[i];
				arr[i] = arr[j];
				arr[j] = temp;
				String temp1 = content[i];
				content[i] = content[j];
				content[j] = temp1;
				i++;
				j--;
			}
		}
		// recursively sort two sub parts
		if (low < j)
			quickSortLowToHigh(content, arr, low, j);

		if (high > i)
			quickSortLowToHigh(content, arr, i, high);
	}

}
