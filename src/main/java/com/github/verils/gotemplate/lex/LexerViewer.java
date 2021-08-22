package com.github.verils.gotemplate.lex;

public class LexerViewer {

    private final Item[] items;
    private int index;

    public LexerViewer(Item[] items) {
        this.items = items;
    }


    /**
     * 获取下一个元素，但不移动查找标记
     *
     * @return 下一个元素。第一次执行返回第一个元素，超出最后一个元素后返回null
     */
    public Item getNextItem() {
        if (index < items.length) {
            return items[index];
        }
        return null;
    }

    /**
     * 获取下一个元素，并将查找标记后移一位，直到最后一个元素
     *
     * @return 下一个元素。第一次执行返回第一个元素，超出最后一个元素后返回null
     */
    public Item getNextItemAndMove() {
        Item item = getNextItem();
        if (item != null) {
            index++;
        }
        return item;
    }

    /**
     * 获取下一个非空白元素，但不移动查找标记
     *
     * @return 下一个元素。第一次执行返回第一个元素，超出最后一个元素后返回null
     */
    public Item getNextNonSpaceItem() {
        int count = 0;
        while (true) {
            Item item = getNextItemAndMove();
            count++;
            if (item == null) {
                return null;
            }
            if (item.type() != ItemType.SPACE) {
                index -= count;
                return item;
            }
        }
    }

    /**
     * 获取下一个非空白元素，并将查找标记后移到这个元素后，直到最后一个元素
     *
     * @return 下一个元素。第一次执行返回第一个元素，超出最后一个元素后返回null
     */
    public Item getNextNonSpaceItemAndMove() {
        while (true) {
            Item item = getNextItemAndMove();
            if (item == null) {
                return null;
            }
            if (item.type() != ItemType.SPACE) {
                return item;
            }
        }
    }

    /**
     * 获取上一个元素
     *
     * @return 上一个元素。第一次执行返回null，超出最后一个元素后执行返回最后的元素
     */
    public Item getPrevItemAndMove() {
        if (index > 0) {
            return items[--index];
        }
        return null;
    }

    /**
     * 获取上一个非空白元素
     *
     * @return 上一个元素。第一次执行返回null，超出最后一个元素后执行返回最后的元素
     */
    public Item getPrevNonSpaceItemAndMove() {
        while (true) {
            Item item = getPrevItemAndMove();
            if (item == null) {
                return null;
            }
            if (item.type() != ItemType.SPACE) {
                return item;
            }
        }
    }
}
