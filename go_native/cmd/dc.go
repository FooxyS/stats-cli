package main

/*
#include <stdint.h> // импортирует типы, которые используются для переноса кода между разными платформами.
*/
import "C"

import (
	"fmt"
	"runtime/cgo"
	"unsafe"

	"github.com/axiomhq/hyperloglog" // подключаем HLL
)

// структура состояния, в которой будут накапливаться хэши уникальных значений
type dcState struct {
	hll *hyperloglog.Sketch
}

func newDC() *dcState {
	fmt.Println("вызвался newDC на стороне go")
	sk := hyperloglog.New16()
	return &dcState{hll: sk}
}

func (s *dcState) addBatch(xs []uint64) {
	fmt.Println("вызвался addBatch на стороне go")
	for _, x := range xs {
		s.hll.InsertHash(x)
	}
}

/*
func (s *dcState) combine(o *dcState) {
 // TODO: реализовать метод combine для группировки
}
*/

func (s *dcState) finish() uint64 {
	fmt.Println("вызвался finish на стороне go")
	return uint64(s.hll.Estimate())
}

//export DcInit
func DcInit() C.uintptr_t {
	fmt.Println("вызвался DcInit на стороне go")
	h := cgo.NewHandle(newDC())
	return C.uintptr_t(uintptr(h))
}

//export GetHashBatch
func GetHashBatch(h C.uintptr_t, data *C.longlong, n C.size_t) {
	fmt.Println("вызвался GetHashBatch на стороне go")
	if data == nil || n == 0 {
		return
	}
	st := cgo.Handle(uintptr(h)).Value().(*dcState)
	sl := unsafe.Slice((*uint64)(unsafe.Pointer(data)), int(n))
	st.addBatch(sl)
}

/*
//export DcCombine
func DcCombine(dst, src C.uintptr_t) {
	// TODO: реализовать, когда будет группировка
}
*/

//export DcFinish
func DcFinish(h C.uintptr_t) C.ulonglong {
	fmt.Println("вызвался DcFinish на стороне go")
	st := cgo.Handle(uintptr(h)).Value().(*dcState)
	return C.ulonglong(st.finish())
}

func main() {}
