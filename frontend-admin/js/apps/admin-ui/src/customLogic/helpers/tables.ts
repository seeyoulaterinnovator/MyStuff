import { AutomaticallyMergedColumn } from "../types/table";
import { getRangeArray } from "./arrays";
import { getAttributeName } from "./attributes";
import { DataAttribute } from "../constants/attributes";

export const getColumnCell = <Data>(
  row: HTMLTableRowElement,
  targetColumn: AutomaticallyMergedColumn<Data>,
) => {
  let resultCell = null;

  if (typeof targetColumn === "number") {
    resultCell = row.cells[targetColumn];
  } else {
    const cellIndex = Array.from(row.cells).findIndex((cell) => {
      return (
        cell.getAttribute(getAttributeName(DataAttribute.TableColumnName)) ===
        targetColumn
      );
    });

    resultCell = row.cells[cellIndex];
  }

  return resultCell;
};

export const mergeCellsAutomatically = <Data>(
  table: HTMLTableElement,
  targetColumns: Array<AutomaticallyMergedColumn<Data>>,
) => {
  let numberOfRowMerges = 0;
  const rows = Array.from(table.tBodies[0].rows);

  // * Объект, создержащий пары "индекс строки: номер группы" для первой группировки
  const rowGroupNumber: Record<number, number> = {};

  // * Массив индексов строк для каждой группы из предыдущей группировки
  // Например: [[1,2][4,5,6]] - первая группа - строки 1-2, вторая группа - строки 4-6
  let rowIndexesParentGroups: number[][] = [getRangeArray(0, rows.length - 1)];

  // * Перебор колонок, подлежащих группировке
  targetColumns.forEach((targetColumn, targetColumnIndex) => {
    // * Массив индексов строк для каждой группы текущей колонки
    const rowIndexesCurrentGroups: number[][] = [];
    let columnGroupIndexes: number[] = [0];

    // * Перебор групп предыдущей группировки
    rowIndexesParentGroups.forEach((rowIndexesParentGroup) => {
      const isMainGroup = targetColumnIndex === 0;
      let prevCell: HTMLTableCellElement | null = null;
      let currentGroupNumber = -1;

      // * Перебор строк группы предыдущей группировки
      rowIndexesParentGroup.forEach((rowIndex) => {
        const row = rows[rowIndex];
        const currentCell = getColumnCell(row, targetColumn);

        if (!currentCell) {
          return;
        }
        console.log(currentCell?.innerText, prevCell?.innerText);

        if (prevCell != null && currentCell.innerText === prevCell.innerText) {
          if (isMainGroup) {
            numberOfRowMerges += 1;
          }

          if (currentCell.style.display !== "none") {
            prevCell.rowSpan = prevCell.rowSpan + 1;
            columnGroupIndexes.push(rowIndex);
            currentCell.style.display = "none";
          }
        } else {
          if (rowIndex !== 0) {
            rowIndexesCurrentGroups.push([...columnGroupIndexes]);
            columnGroupIndexes = [rowIndex];
          }

          prevCell = currentCell;
          currentGroupNumber += 1;
        }

        const isLastElementInGroup =
          rowIndex === rowIndexesParentGroup[rowIndexesParentGroup.length - 1];

        if (isLastElementInGroup) {
          rowIndexesCurrentGroups.push([...columnGroupIndexes]);
          columnGroupIndexes = [0];
        }

        if (isMainGroup) {
          rowGroupNumber[rowIndex] = currentGroupNumber;
        }

        // * Для каждой строки устанавливаем номер главной группы, в которой находится строка
        Array.from(row.cells).forEach((cell) => {
          cell.setAttribute(
            getAttributeName(DataAttribute.TableRowGroupId),
            `${rowGroupNumber[rowIndex]}`,
          );
        });
      });
    });

    rowIndexesParentGroups = [...rowIndexesCurrentGroups];
  });

  return { numberOfRowMerges };
};
