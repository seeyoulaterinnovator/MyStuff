type ResponseStatus = 'SUCCESS' | 'FAIL' | 'ERROR';

export type ConditionalResponseStatus<StatusField extends 'httpStatus' | 'status'> =
	StatusField extends 'status'
		? { status: ResponseStatus }
		: { httpStatus: ResponseStatus };

export type Response<
	Result,
	StatusField extends 'httpStatus' | 'status' = 'status',
> = ConditionalResponseStatus<StatusField> & {
	results: Result;
};
